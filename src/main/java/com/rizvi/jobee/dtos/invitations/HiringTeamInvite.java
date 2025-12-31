package com.rizvi.jobee.dtos.invitations;

import com.rizvi.jobee.entities.HiringTeam;
import com.rizvi.jobee.entities.Invitation;

public record HiringTeamInvite(HiringTeam hiringTeam, Invitation invitation) {
}
