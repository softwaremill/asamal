package pl.softwaremill.asamal.example.service.email;

import pl.softwaremill.common.sqs.task.SQSTaskTimerBean;

import javax.ejb.Stateless;

@Stateless
public class EmailSendingBean extends SQSTaskTimerBean { }