package com.optimised.cylonbackup.data.service;

/**
 * Created by Olga on 8/22/2016.
 */
public interface EmailService {

    boolean sendSimpleMessage(String to,
                           String subject,
                           String text);

    boolean sendMessageWithAttachment(String to,
                                      String subject,
                                      String text,
                                      String pathToAttachment);
}