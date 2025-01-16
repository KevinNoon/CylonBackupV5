package com.optimised.cylonbackup.data.service;

import com.optimised.cylonbackup.data.entity.Logs;
import com.optimised.cylonbackup.data.repository.LogRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {

  final LogRepo logRepo;

  public LogService(LogRepo logRepo) {
    this.logRepo = logRepo;
  }

  public List<Logs> findAll(){
    return logRepo.findAll();
  }

  public List<Logs> findErrorLogs(Long lastRecordNo){
    return logRepo.findErrorLogs(lastRecordNo);
  }

  public List<Logs> findByLevelAndMessage(String searchLevel, String searchMessage){
    if ((searchLevel.isEmpty() && searchMessage.isEmpty())) {
      return logRepo.findAllByOrderByLogDateDesc();
    } else {
      return logRepo.filterByLevelAndMessage(searchLevel,searchMessage);
    }
  }
}
