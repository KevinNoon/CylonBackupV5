package com.optimised.cylonbackup.data.service;

import com.optimised.cylonbackup.data.entity.Engineer;
import com.optimised.cylonbackup.data.repository.EngineerRepo;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class EngineerService {
    private final EngineerRepo engineerRepo;
    final static Marker DB = MarkerManager.getMarker("DB");

    public EngineerService(EngineerRepo engineerRepo) {
        this.engineerRepo = engineerRepo;
    }

    public void saveEngineer(Engineer engineer){
        Optional<Engineer> dbEngineer = Optional.empty();
        if (engineer.getId() != null) {
            dbEngineer = engineerRepo.findById(engineer.getId());
        }
            if (dbEngineer.isPresent()) {
                Engineer dbE = dbEngineer.get();
                if (!dbE.getFullName().equals(engineer.getFullName())) {
                    log.info(DB, "{}{}{}{}", "Engineer Name: ", dbE.getFullName(), " changed to ", engineer.getFullName());
                }
                if (!dbE.getEmail().equals(engineer.getEmail())) {
                    log.info(DB, "{}{}{}{}", "Engineer Email : ", dbE.getEmail(), " changed to ", engineer.getEmail());
                }

            } else {
                log.info(DB, "{}{}", "Engineer Added ", engineer.getFullName());
        }
        engineerRepo.save(engineer);

    }

    public List<Engineer> findAllEngineers(){
            return engineerRepo.findAll();
    }

    public void delete(Long id) {
        Optional<Engineer> engineer = engineerRepo.findById(id);
        if (engineer.isPresent()){
            engineerRepo.deleteById(id);
            log.info(DB,"{}{}","Engineer Deleted " , engineer.get().getFullName());
        }
    }

    public Optional<Engineer> findById(Long id){
        return engineerRepo.findById(id);
    }

    public Optional<Engineer> findByFullName(String foreName, String lastName) {
        return engineerRepo.findByForenameAndLastname(foreName,lastName);
    }
}
