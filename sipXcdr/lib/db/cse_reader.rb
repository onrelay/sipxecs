#
# Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
# Contributors retain copyright to elements licensed under a Contributor Agreement.
# Licensed to the User under the LGPL license.
#
##############################################################################
require 'pg'
require 'call_state_event'
require 'db/dao'
require 'utils/terminator'

# Obtains CSEs from DB and puts them into CSE queue
class CseReader < Dao
  MAX_CSES = 1500

  def initialize(database_url, purge_age, polling_interval, log = nil)
    super(database_url, purge_age, 'call_state_events', log)
    @last_read_id = nil
    @last_read_time = nil
    @last_cse_event_time = nil
    log.debug("cse_reader.rb:: Polling CSE DB every #{polling_interval} seconds.") if log
    @poll_time = polling_interval
    @synch_time = [120, @poll_time].max
    @synch_timeslice = @synch_time / @poll_time
    @database_url = database_url
    @log = log
    @stop = Terminator.new(polling_interval)
  end

  def run(cse_queue, start_id, start_time, stop_time = nil)
    @log.debug("cse_reader.rb:: Connecting to CSE database: #{@database_url}") if @log
    connect do |conn|
      if stop_time
        check_purge(conn)
        read_cses(conn, cse_queue, nil, start_time, stop_time)
      else
        @last_read_time ||= start_time
        @last_read_id ||= start_id
        @last_cse_loop_read = 0
        loop do
          check_purge(conn)
          first_id = @last_read_id
          first_time = @last_read_time unless first_id
          @log.debug("cse_reader.rb:: Read CSEs from ID:#{first_id} Time:#{first_time}") if @log
          read_cses(conn, cse_queue, first_id, first_time, nil)

          if (first_id.to_i + MAX_CSES) > @last_read_id.to_i
            @log.debug("cse_reader.rb:: Going to sleep.") if @log
            @stop.wait
            @log.debug("cse_reader.rb:: Waking up.") if @log
          end
        end
      end
    end
  rescue PG::Error => excp
    @log.error("cse_reader.rb:: Loss of connection to database #{@database_url} - retrying after sleep") if @log
    retry if !@stop.wait
  rescue => e
    @log.error("cse_reader.rb:: Exception in reader thread: <#{e}> - Database = #{@database_url}") if @log
    Thread.current[:exception] = e
    raise
  ensure
    @log.debug("cse_reader.rb:: Stopping CSE reader.") if @log
  end

  def stop
    @log.debug("cse_reader.rb:: CSE Reader - calling stop") if @log
    @stop.stop
  end

  def read_cses(conn, cse_queue, start_id, start_time, stop_time)
    sql, params = self.class.select_sql(start_id, start_time, stop_time)
    reccount = 0
    conn.exec_params(sql, params) do |result|
      result.each do |row|
        cse = self.class.cse_from_row(row)
        @last_read_id = cse.id
        reccount += 1
        @last_read_time = nil
        @last_cse_event_time = cse.event_time
        cse_queue << cse
      end
    end

    if reccount == 0
      @last_cse_loop_read += 1
      if @last_cse_loop_read >= @synch_timeslice && @last_cse_event_time
        cse = CallStateEvent.new
        @last_cse_event_time = cse.event_time = @last_cse_event_time.to_time + @synch_time
        @log.debug("cse_reader.rb:: Injecting synchronize event: new time #{cse.event_time}") if @log
        cse_queue << cse
        @last_cse_loop_read = 0
      end
    else
      @last_cse_loop_read = 0
    end
  end

  def purge_now(conn, start_time_cse)
    @log.debug("cse_reader.rb:: Purging CSEs older than #{start_time_cse}") if @log
    sql, params = self.class.delete_sql(start_time_cse)
    conn.exec_params(sql, params)
  end

  class << self
    def cse_from_row(row)
      cse = CallStateEvent.new
      CallStateEvent::FIELDS.each do |field|
        setter = "#{field}=".to_sym
        cse.send(setter, row[field.to_s])
      end
      cse
    end

    def select_sql(start_id = nil, start_time = nil, end_time = nil)
      field_names = CallStateEvent::FIELDS.map(&:to_s).join(', ')
      sql = "SELECT #{field_names} FROM call_state_events"
      sql, params = append_where_clause(sql, start_id, start_time, end_time)
      sql += " ORDER BY event_time LIMIT #{MAX_CSES}"
      [sql, params]
    end

    def delete_sql(start_time)
      sql = "DELETE FROM call_state_events"
      sql, params = append_where_clause(sql, nil, start_time, nil)
      [sql, params]
    end

    def append_where_clause(sql, start_id, start_time, end_time)
      conditions = []
      params = []

      if start_id
        conditions << "id > $#{params.size + 1}"
        params << start_id
      end
      if start_time
        conditions << "event_time >= $#{params.size + 1}"
        params << start_time
      end
      if end_time
        conditions << "event_time <= $#{params.size + 1}"
        params << end_time
      end

      sql += " WHERE #{conditions.join(' AND ')}" unless conditions.empty?
      [sql, params]
    end
  end
end