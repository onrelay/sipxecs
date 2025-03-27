#
# Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
# Contributors retain copyright to elements licensed under a Contributor Agreement.
# Licensed to the User under the LGPL license.
#
##############################################################################

require 'test/unit'
require 'thread'
require 'pg'

$:.unshift File.join(File.dirname(__FILE__), '..', 'lib')
require 'state'

$:.unshift File.join(File.dirname(__FILE__), '..', 'test')
require 'test_util'
include TestUtil

class StateTest < Test::Unit::TestCase
  class DummyCdr
    attr_reader :counter, :call_id, :callee_aor, :reference

    def initialize(call_id, log = nil, callee_aor = "sip:221@example.com")
      @counter = 0
      @call_id = call_id
      @callee_aor = callee_aor
    end

    def accept(cse)
      @counter += 1
      return self if @counter > 1
    end

    def terminated?
      true
    end

    def retire; end
  end

  class DummyCse < CallStateEvent
    attr_reader :id, :call_id, :event_time, :event_type

    def initialize(call_id, event_time = Time.new(2000, 1, 1, 1, 1, 0), event_type = 'R')
      @call_id = call_id
      @event_time = event_time
      @event_type = event_type
      @id = 1
    end
  end

  def test_empty
    q1 = Queue.new
    q2 = Queue.new
    t = Thread.new(State.new(q1, q2)) { |s| s.run }
    q1.enq(nil)
    t.join
  end

  def test_accept
    observer = DummyQueue.new
    cse1 = DummyCse.new('id1')
    cse2 = DummyCse.new('id2')

    state = State.new([], observer, DummyCdr)

    state.accept(cse1)
    assert_equal(0, observer.counter)

    state.accept(cse2)
    assert_equal(0, observer.counter)

    state.accept(cse1)
    assert_equal(1, observer.counter)

    state.accept(cse2)
    assert_equal(2, observer.counter)
  end

  def test_retire_long_calls
    out_queue = []
    cse1 = DummyCse.new('id1', Time.new(2000, 1, 1, 10, 1, 1))
    cse2 = DummyCse.new('id2', Time.new(2000, 1, 1, 10, 2, 41)) # +100 seconds
    cse3 = DummyCse.new('id3', Time.new(2000, 1, 1, 10, 4, 21)) # +200 seconds
    in_queue = [cse1, cse2, cse3, [:retire_long_calls, 150]]

    MockCdr.results(false, false, false, false, false, false)
    state = State.new(in_queue, out_queue, MockCdr)
    state.run

    assert_equal(2, out_queue.size)
    assert_nil(out_queue[1])
    assert_equal(2, state.active_cdrs.size)
  end

  def test_flush_failed_calls
    out_queue = []
    MockCdr.results(true, false, false)
    state = State.new([], out_queue, MockCdr)
    state.accept(DummyCse.new('id1', Time.new(2000, 1, 1, 10, 1, 0)))
    state.accept(DummyCse.new('id2', Time.new(2000, 1, 1, 10, 3, 31)))
    assert_equal(0, out_queue.size)

    state.flush_failed_calls(160)
    assert_equal(0, out_queue.size)

    state.flush_failed_calls(150)
    assert_equal(1, out_queue.size)
  end
end