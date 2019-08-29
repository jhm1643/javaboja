package api.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import api.domain.QueueReceiver;
import api.push.PushService;
 
@EnableScheduling
@Component
public class SchedulerTask {
	
	@Autowired
	private ApplicationContext ctx;
	
	@Autowired
	private QueueReceiver qr;
	
	private static Logger logger = LoggerFactory.getLogger(SchedulerTask.class);
	
	///////////////////////////////////////////////////////////
	//2018.11.01 martino Push Service를 위해 schedule time을 100ms -> 10ms로 조정
	//					 qr.receiveMessage()에서 RabbitMQ에서 message를 연속적으로 읽음	
	//@Scheduled(fixedDelay=100)
	@Scheduled(fixedDelay=50)
	public void readQueue() throws Exception {
//		logger.info(".....................................................");
			
		//2019.01.15 martino Push Service는 ucex에서 실행하지 않음
		//qr.receiveMessage();
	}
	
	// 매일 새벽 2시 7분에 push result 테이블을 정리해준다
	@Scheduled(cron = "${push.delete.log.cron}")
	public void deleteOldPushLogs() throws Exception {
		logger.info("SCHEDULER [deleteOldPushLogs]");
		PushService ps = (PushService) ctx.getBean("pushService");
		ps.deleteOldPushLogs();
	}
	
	/*@Scheduled(cron = "${push.apns.feedback.cron}")
	public void feedbackAPNS() throws Exception {
		logger.info("SCHEDULER [APNS FEEDBACK]");
		PushService ps = (PushService) ctx.getBean("pushService");
		ps.feedbackAPNS();
	}*/

}