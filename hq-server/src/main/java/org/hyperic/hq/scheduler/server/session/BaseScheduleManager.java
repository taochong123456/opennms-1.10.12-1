/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 *
 * Copyright (C) [2004-2008], Hyperic, Inc.
 * This file is part of HQ.
 *
 * HQ is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.hyperic.hq.scheduler.server.session;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;

import org.hyperic.hq.appdef.shared.AppdefEntityID;
import org.hyperic.hq.authz.server.session.AuthzSubject;
import org.hyperic.hq.common.SystemException;
import org.hyperic.util.StringUtil;
import org.hyperic.util.jdbc.DBUtil;
import org.hyperic.util.pager.Pager;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

/**
 * Implements common functionality shared by various schedule managers, for
 * example the ControlScheduleManager and AIScheduleManager session beans.
 */
public abstract class BaseScheduleManager {

    public static final String SCHED_SEPARATOR = "-";
    private int dbType;

    protected Scheduler scheduler;

    private String jobPrefix;
    private String schedulePrefix;

    protected Pager historyPager;
    protected Pager schedulePager;

    // Subclasses implement these method so that we can
    // do a lot of stuff uniformly at this common base-class level.
    protected abstract String getHistoryPagerClass();

    protected abstract String getSchedulePagerClass();

    protected abstract String getJobPrefix();

    protected abstract String getSchedulePrefix();

    protected DBUtil dbUtil;

    public BaseScheduleManager(Scheduler scheduler, DBUtil dbUtil) {
        this.scheduler = scheduler;
        this.dbUtil = dbUtil;
    }

    // Helper methods
    protected String getPrefix(AppdefEntityID id) {
        return id.getID() + SCHED_SEPARATOR + id.getType();
    }

    protected String getJobName(AuthzSubject subject, AppdefEntityID id, String instanceIdentifier) {
        return jobPrefix + SCHED_SEPARATOR + getPrefix(id) + SCHED_SEPARATOR + instanceIdentifier + SCHED_SEPARATOR +
               System.currentTimeMillis();
    }

    protected String getTriggerName(AuthzSubject subject, AppdefEntityID id, String instanceIdentifier) {
        return schedulePrefix + SCHED_SEPARATOR + getPrefix(id) + SCHED_SEPARATOR + instanceIdentifier +
               SCHED_SEPARATOR + System.currentTimeMillis();
    }

    protected void setupJobData(JobDetail jobDetail, AuthzSubject subject, AppdefEntityID id, String scheduleString,
                                Boolean scheduled, int[] order) {
        JobDataMap dataMap = jobDetail.getJobDataMap();
        dataMap.put(BaseJob.PROP_ID, id.getId().toString());
        dataMap.put(BaseJob.PROP_TYPE, new Integer(id.getType()).toString());
        dataMap.put(BaseJob.PROP_SUBJECT, subject.getId().toString());
        dataMap.put(BaseJob.PROP_SCHEDULESTRING, scheduleString);
        dataMap.put(BaseJob.PROP_SCHEDULED, scheduled.toString());
        String orderStr = "";
        if (order != null)
            orderStr = StringUtil.arrayToString(order);
        dataMap.put(BaseJob.PROP_ORDER, orderStr);
    }

    @PostConstruct
    protected void initialize() {
        try {
            // Setup the pagers
            this.historyPager = Pager.getPager(getHistoryPagerClass());
            this.schedulePager = Pager.getPager(getSchedulePagerClass());

            // Note the type of database
            setDbType();
        } catch (Exception e) {
            // IllegialAccessException, InvocationException..
            throw new SystemException(e);
        }

        this.jobPrefix = getJobPrefix();
        this.schedulePrefix = getSchedulePrefix();
    }

    protected int getDbType() {
        return this.dbType;
    }

    protected void setDbType() {
        Connection conn = null;
        try {
            conn = dbUtil.getConnection();
            this.dbType = DBUtil.getDBType(conn);
        } catch (SQLException e) {
            // log and continue
        } finally {
            DBUtil.closeConnection(null, conn);
        }
    }
}
