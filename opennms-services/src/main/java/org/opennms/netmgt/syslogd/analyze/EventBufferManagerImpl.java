package org.opennms.netmgt.syslogd.analyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class EventBufferManagerImpl implements EventBufferManager {

	public static int MAX_READNUMBER = 10000;// 最大读取行数

	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * 启动本事件缓冲管理器的服务例程.
	 */
	private RuleEngineService service;

	/**
	 * 每个数组的长度。
	 */
	public int MAX_ARRAY_ELEMENTNUMBER = 500;

	/**
	 * 当收入的事件数目为BREAK_COUNT，系统停1ms
	 */
	public int BREAK_COUNT = MAX_ARRAY_ELEMENTNUMBER * 100;

	/**
	 * 允许的最多数组个数。MAX_ARRAY_NUMBER*MAX_ARRAY_ELEMENTNUMBER是内存中允许保留的最多事件对象个数。
	 */
	public int MAX_ARRAY_NUMBER = 200;
	/**
	 * 当前工作数组中已经收到的事件对像的数组下标。
	 */
	private int headBlockPoint;

	/**
	 * 当前工作数组的描述对象。
	 */
	private EventBlockBuffer headBlock;

	/**
	 * 尾数组的描述对象。
	 */
	private EventBlockBuffer tailBlock;

	/**
	 * 当前已经分配的数组缓冲数目。
	 */
	private int currentBufferNumber;

	/**
	 * 自服务启动以来，接收到的事件数目。
	 */
	private long eventNo;

	/**
	 * 向缓存中添加事件对像时的锁定。
	 */
	private final ReentrantLock enterLock = new ReentrantLock();

	/**
	 * 系统采用的等级数目，目前一共5级，从0-4
	 */
	private int priorityNumber = 5;

	/**
	 * 构造函数
	 * 
	 * @param service
	 */
	public EventBufferManagerImpl(RuleEngineService service) {
		this.service = service;
		headBlockPoint = MAX_ARRAY_ELEMENTNUMBER;

		try {// 根据配置文件中需要缓存的日志数目，计算需要的最大数组的值。
			long logCount = Long.parseLong(System
					.getProperty("SIMEventMemoryCacheSize"));
			if (logCount > 10000) {
				MAX_ARRAY_NUMBER = (int) Math.ceil((double) logCount
						/ (double) MAX_ARRAY_ELEMENTNUMBER);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 创建新的事件数组节点
	 * 
	 */
	private void makeNewEventBuffer(long currentEventReceptTime) {

		// -- 创建新的数组描述对象 --
		EventBlockBuffer newBlock = new EventBlockBuffer();
		newBlock.receptTime = currentEventReceptTime;

		// -- 创建新的数组空间 --
		newBlock.buffer = new SIMEventObject[MAX_ARRAY_ELEMENTNUMBER];

		// -- 向双向环链表中插入新节点 --
		if (headBlock != null) {

			newBlock.pre = headBlock;
			newBlock.next = tailBlock;

			headBlock.next = newBlock;
			tailBlock.pre = newBlock;

		} else {// 第一个缓冲区块
			newBlock.pre = newBlock;
			newBlock.next = newBlock;
			headBlock = newBlock;
			tailBlock = newBlock;
		}

		// -- 修改链表头尾指针 --
		headBlock = newBlock;

		// -- 更新headBlockPoint，准备重新计数 --
		headBlockPoint = 0;

		// -- 增加缓冲数组个数的计数 --
		currentBufferNumber++;
	}

	/**
	 * 释放缓存空间，释放掉指定缓冲块
	 * 
	 * @param block
	 *            需要被释放的缓冲块。
	 */
	private void releaseBlockBuffer(EventBlockBuffer block) {
		if (currentBufferNumber == 1)// 系统保证至少还存在一块缓冲区
			return;

		if (block != null) {

			EventBlockBuffer newNextBlock = block.next;
			EventBlockBuffer newPreBlock = block.pre;

			// -- 去除block的双向连接关系 --
			if (newNextBlock != null && newPreBlock != null) {

				newNextBlock.pre = newPreBlock;
				newPreBlock.next = newNextBlock;

				block.pre = null;
				block.next = null;

				if (block == tailBlock) {
					tailBlock = newNextBlock;
					// tailBlock = newPreBlock;
				}
				if (block == headBlock) {
					headBlock = newPreBlock;
				}

				currentBufferNumber--;
			}

		}
	}

	// long count = 0;

	/**
	 * 添加事件对像。
	 */
	public boolean addEvent(SIMEventObject event) {
		if (event == null)
			return false;

		try {
			enterLock.lock();
			if (headBlockPoint == MAX_ARRAY_ELEMENTNUMBER) {

				if (currentBufferNumber >= MAX_ARRAY_NUMBER) {
					// -- 如果超过允许的最大内存容量，释放尾部空间 --
					releaseBlockBuffer(tailBlock);
				}
				// -- 创建新的事件数组节点 --
				makeNewEventBuffer(event.getReceptTime());
				// System.out.println(currentBufferNumber+" "+eventNo);
			}
			headBlock.buffer[headBlockPoint] = event;

			if (event.receptTime < headBlock.receptTime) {
				headBlock.receptTime = event.receptTime;
				headBlock.index = headBlockPoint;
			}
			headBlockPoint++;
			eventNo++;

			eventNo = eventNo % Long.MAX_VALUE;
			if (eventNo % BREAK_COUNT == 0) {// 接收满BREAK_COUNT个事件后，停一下。让其他线程得到执行的机会。
				try {
					Thread.sleep(1);
				} catch (Exception e) {
				}
			}

			return true;

		} finally {
			enterLock.unlock();
		}

	}

	public List getEvents(long startTime, long endTime) {
		return getEvents(startTime, endTime, null, -1, -1);
	}

	public List getEvents(long startTime, long endTime, String filterID,
			int maxEventCount) {
		return getEvents(startTime, endTime, filterID, -1, maxEventCount);
	}

	public List getEvents(long startTime, String filterID, long eventID,
			int maxEventCount) {
		return getEvents(startTime, System.currentTimeMillis(), filterID,
				eventID, maxEventCount);
	}

	public List getEvents(long startTime, long endTime, String filterID,
			long eventID, int maxEventCount) {
		EventResultSet result = getEventsAndStatistic(startTime, endTime,
				filterID, eventID, -1, maxEventCount);
		return result.getEvents();
	}

	public List getEventsAndCount(long startTime, long endTime,
			String filterID, long startEventID, long endEventID,
			int maxEventCount) {
		EventResultSet result = getEventsAndStatistic(startTime, endTime,
				filterID, startEventID, endEventID, maxEventCount);
		// 将原先result中首元素（是List类型）的最后一个元素取出来，重新设置为该result的第一个元素
		List eventList = new ArrayList();
		eventList.add(result.getTotalEventCount());
		eventList.add(result.getEvents());
		return eventList;
	}

	/**
	 * 计算总流量
	 * 
	 * @param priorityFluxArray
	 * @return
	 */
	private long calculateFluxCount(long[] priorityFluxArray) {
		long total = 0;
		for (int i = 0; i < priorityFluxArray.length; i++) {
			total += priorityFluxArray[i];
		}
		return total;
	}

	/**
	 * 获取事件结果集。参数含义详见EventBufferManager接口说明。
	 */
	public EventResultSet getEventsAndStatistic(long startTime, long endTime,
			String filterID, long startEventID, long endEventID,
			int maxEventCount) {

		// List resultE = new ArrayList();

		long totalEventCount = 0; // 事件总数计数
		long totalFluxCount = 0;// 事件总流量

		long[] priorityArray = new long[priorityNumber + 1];// 事件等级计数,priorityArray[0]记录等级为0的事件数,...priorityArray[4]记录等级为4的事件数
		// 最后一个元素记录等级以外的事件数量，这说明归一化有问题
		Arrays.fill(priorityArray, 0);

		long[] priorityFluxArray = new long[priorityNumber + 1];// 事件等级流量计数,priorityFluxArray[0]记录等级为0的事件流量,...priorityFluxArray[4]记录等级为4的事件流量
		// 最后一个元素记录等级以外的事件流量，这说明归一化有问题
		Arrays.fill(priorityFluxArray, 0);

		// 事件结果列表
		List<SIMEventObject> events = new ArrayList<SIMEventObject>();

		EventResultSet ers = new EventResultSet();// 设置ers的数据结构
		ers.setPriorityEventCount(priorityArray);
		ers.setPriorityFluxCount(priorityFluxArray);
		ers.setEvents(events);

		// -- 如果时间参数有问题，则返回0个记录 --
		if (endTime < startTime) {
			return ers;
		}

		boolean canProcess = endEventID < 0 ? true : false; // 是否可以继续处理事件的标志。实际上表达如果需要找寻endEventID之前的操作，则需要判定什么时候开始此操作。

		if (maxEventCount < 0) {
			maxEventCount = Integer.MAX_VALUE;
			maxEventCount = MAX_READNUMBER;// 目前暂时做了一下限制。
		}

		// -- 设置工作缓冲区指针 --
		EventBlockBuffer workBuffer = headBlock;
		do {
			// -- 以防万一，如果缓冲区链断开，则退出查找 --
			if (workBuffer == null) {
				logger.info("No Event received now.");
				break;
			}

			if (workBuffer.receptTime <= endTime) {// -- 这里是"<="
				// -- 当前块中，包含所要找的符合时间范围的事件对像 --
				for (int i = MAX_ARRAY_ELEMENTNUMBER - 1; i >= 0; i--) {
					SIMEventObject event = workBuffer.buffer[i];
					if (event != null
							&& (event.receptTime >= startTime && event.receptTime <= endTime)) {

						if (!canProcess) {// 说明endEventID>0,并且还没有发现该endEventID
							if (event.getID() == endEventID) {
								canProcess = true;
							}
							continue;// 注意此
							// continue的位置，在event.getID()==endEventID的判断之外
						}

						if (startEventID > 0 && event.getID() == startEventID)// 如果发现了指定的startEventID，则不再继续往下查找。
							break;

						// -- 如果需要进行过滤器判断，则验证它 --
						boolean accept = true;
						if (filterID != null && filterID.trim().length() != 0) {
							accept = filter(filterID, event);
						}

						// -- 添加到结果集 --
						if (accept) {
							if (totalEventCount < maxEventCount)
								events.add(event);
							totalEventCount++;
							Integer priority = event.getPriority();
							int position = priority == null || priority < 0
									|| priority >= priorityNumber ? priorityNumber
									: priority; // 判断统计数据数组下标的位置，如果等级超过了正常范围，则将其置为priorityNumber
							priorityArray[position]++;
							priorityFluxArray[position] += (event.getSend() != null ? event
									.getSend().longValue() : 0)
									+ (event.getReceive() != null ? event
											.getReceive().longValue() : 0);// 流量计数累加
						}
					}
				}

				if (workBuffer.receptTime < startTime) {// --这里是 "<",
					// 当workBuffer.receptTime==startTime时，还需要继续向前查找
					ers.setTotalEventCount(totalEventCount);
					ers.setTotalFluxCount(calculateFluxCount(priorityFluxArray));
					return ers;
				}
			}

			workBuffer = workBuffer.pre;

		} while (workBuffer != headBlock && workBuffer != null);// -- 转了一圈 --

		// -- 设置结果集 --
		ers.setTotalEventCount(totalEventCount);
		ers.setTotalFluxCount(calculateFluxCount(priorityFluxArray));
		return ers;
	}

	/**
	 * 获得缓存中时间最近的事件对象。
	 * 
	 * @return
	 */
	public SIMEventObject getRecentEvent() {
		if (headBlock != null) {
			try {
				return headBlock.buffer[headBlock.index];
			} catch (Exception e) {
			}

		}
		return null;
	}

	/**
	 * 获得缓存中时间最早的事件对象。
	 * 
	 * @return
	 */
	public SIMEventObject getLastEvent() {
		if (tailBlock != null) {
			try {
				return tailBlock.buffer[tailBlock.index];
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * 获取下一个事件代理。
	 */
	public SIMEventProxy getNextEvent(SIMEventProxy current) {

		if (current == null)
			current = new SIMEventProxy();

		if (current.getBuffer() == null) {
			if (tailBlock != null) {// 首次查找事件;或事件接收的太快，current中的buffer已经被回收[目前只有tailBlock才会被回收],则将buffer指向新的tailBlock
				current.setBuffer(tailBlock);
				current.setEvent(tailBlock.buffer[0]);
				current.setPosition(0);
			}
		} else {
			EventBlockBuffer currentBlockBuffer = (EventBlockBuffer) current
					.getBuffer();
			int currentPosition = current.getPosition();

			if (current.getEvent() == null) {
				// 继续查看当前位置。
				current.setEvent(currentBlockBuffer.buffer[currentPosition]);

			} else {
				// 获取下一个。
				currentPosition++;

				if (currentPosition < currentBlockBuffer.buffer.length) {

					current.setEvent(currentBlockBuffer.buffer[currentPosition]);
					current.setPosition(currentPosition);

				} else {// 到了当前缓冲区的最后一个位置
					if (currentBlockBuffer != headBlock) {// 如果当前缓冲区不是headBlock，说明缓冲区接收事件的速度已经超过了规则判断的速度。

						currentBlockBuffer = currentBlockBuffer.next;// 转移到下一个缓冲区[事件发生时间较晚的相邻缓冲区]
						// System.out.println(currentBlockBuffer);
						if (currentBlockBuffer != null) {// 如果为null,则说明缓冲区的链断了，不做处理，下一次getNextEvent调用会自动从tailBlock重新开始。

							currentPosition = 0;
							current.setEvent(currentBlockBuffer.buffer[currentPosition]);
							current.setPosition(currentPosition);
						}

						current.setBuffer(currentBlockBuffer);

					} else {// 停在了headBlock最后一个事件对像之后，说明已经没有新的事件需要处理。

						currentPosition = currentBlockBuffer.buffer.length;

						current.setPosition(currentPosition);

					}
				}

			}
		}

		return current;
	}

	/**
	 * 使用指定filterID对应的过滤器，判断event是否满足过滤条件
	 * 
	 * @param filterID
	 * @param event
	 * @return
	 */
	boolean filter(String filterID, SIMEventObject event) {

		FilterManager fm = service.getFilterManager();

		if (fm == null)
			return false;

		FilterDelegate fd = fm.getFilterDelegate(filterID);
		if (fd == null)
			return false;

		return fd.isSatisfied(event);
	}

	static class Query extends Thread {
		EventBufferManager eb;

		public Query(final EventBufferManager eb) {
			this.eb = eb;
		}

		public void run() {
			while (true) {
				long start = System.currentTimeMillis();
				List result = eb.getEventsAndCount(
						System.currentTimeMillis() - 1000 * 10,
						System.currentTimeMillis(), null, -1, -1, 50);
				int count = (Integer) result.get(0);
				long end = System.currentTimeMillis();
				System.out.println(Thread.currentThread().getName()
						+ "query time:" + (end - start) + "ms  count:" + count);
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
			}
		}
	}

	static class EventPut extends Thread {
		EventBufferManager eb;

		public EventPut(final EventBufferManager eb) {
			this.eb = eb;
		}

		public void run() {
			long start = System.currentTimeMillis();
			long ee = System.currentTimeMillis();
			long i = 0;
			while (true) {
				SIMEventObject so = new SIMEventObject();
				so.setPriority(1);
				so.setReceptTime(System.currentTimeMillis());
				so.setMsg(String.valueOf(i++));
				eb.addEvent(so);
			}
		}
	}

	public long getEventNo() {
		return eventNo;
	}

}
