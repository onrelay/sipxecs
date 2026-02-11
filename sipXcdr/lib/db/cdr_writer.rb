#
# Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
# Contributors retain copyright to elements licensed under a Contributor Agreement.
# Licensed to the User under the LGPL license.
#
##############################################################################
require 'pg'
require 'cdr'
require 'db/dao'

# Writes CDRs to the database
class CdrWriter < Dao
  def initialize(database_url, purge_age, log = nil)
    super(database_url, purge_age, 'cdrs', log)
  end

  def run(queue)
    connect do |conn|
      sql = CdrWriter.insert_sql
      @log.debug("cdr_writer.rb:: Preparing SQL: #{sql}") if @log
      conn.prepare('insert_cdr', sql)

      while (cdr = queue.shift)
        if CdrWriter.getRetryCount > 5
          cdr = queue.shift
          @log.warn("cdr_writer.rb:: Database error occurred 5 times successively, skipping to the next CDR") if @log
        end
        row = CdrWriter.row_from_cdr(cdr)
        @last_row = row
        conn.exec_prepared('insert_cdr', row)
        check_purge(conn)
        CdrWriter.cleanRetries
      end
    end
  rescue PG::Error => e
    @log.error("cdr_writer.rb:: values = #{@last_row.join(', ')} ---- Error = #{e.message}") if @log
    CdrWriter.countRetries
    retry
  end

  def last_cdr_start_time
    connect do |conn|
      result = conn.exec(CdrWriter.last_cdr_sql)
      return result.getvalue(0, 0) unless result.ntuples.zero?
      nil
    end
  rescue PG::Error => e
    @log.error("cdr_writer.rb:: Error fetching last CDR start time: #{e.message}") if @log
    nil
  end

  def purge_now(conn, start_time_cdr)
    @log.debug("cdr_writer.rb:: Purging CDRs older than #{start_time_cdr}") if @log
    sql = CdrWriter.delete_sql
    conn.exec_params(sql, [start_time_cdr])
  end

  class << self
    def row_from_cdr(cdr)
      Cdr::FIELDS.map { |f| cdr.send(f) }
    end

    def insert_sql
      field_names = Cdr::FIELDS.map(&:to_s)
      field_str = field_names.join(', ')
      value_str = (1..field_names.size).map { |i| "$#{i}" }.join(', ')
      "INSERT INTO cdrs (#{field_str}) VALUES (#{value_str})"
    end

    def delete_sql
      "DELETE FROM cdrs WHERE start_time < $1"
    end

    def last_cdr_sql
      "SELECT start_time FROM cdrs ORDER BY start_time DESC LIMIT 1"
    end

    @@retryCount = 0

    def countRetries
      @@retryCount += 1
    end

    def cleanRetries
      @@retryCount = 0
    end

    def getRetryCount
      @@retryCount
    end
  end
end
