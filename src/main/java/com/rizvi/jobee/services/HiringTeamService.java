package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.HiringTeam;
import com.rizvi.jobee.repositories.HiringTeamRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class HiringTeamService {
    private final HiringTeamRepository hiringTeamRepository;

    public void linkNonJobeeHiringTeamMemberWithBusinessAccount(BusinessAccount businessAccount) {
        List<HiringTeam> hiringTeams = hiringTeamRepository.findNonJobeeMembersByEmail(businessAccount.getEmail());
        System.out.println("SYED-DEBUG: Found " + hiringTeams.size() + " non-Jobee hiring team members with email "
                + businessAccount.getEmail());
        for (HiringTeam hiringTeam : hiringTeams) {
            System.out.println("SYED-DEBUG: Linking HiringTeam ID " + hiringTeam.getId() + " with BusinessAccount ID "
                    + businessAccount.getId());
            hiringTeam.setBusinessAccount(businessAccount);
            hiringTeamRepository.save(hiringTeam);
        }
    }
}
