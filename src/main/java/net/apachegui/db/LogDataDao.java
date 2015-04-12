package net.apachegui.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import javax.sql.DataSource;

import net.apachegui.global.Constants;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class LogDataDao {
    private static Logger log = Logger.getLogger(LogDataDao.class);

    private JdbcTemplate jdbcTemplate;
    private static LogDataDao logDataDao;

    public void setDataSource(DataSource dataSourceIn) {
        jdbcTemplate = new JdbcTemplate(dataSourceIn);
        logDataDao = this;
    }

    public static LogDataDao getInstance() {
        return logDataDao;
    }

    /**
     * Writes the log data directly to the database.
     * 
     * @param data
     *            - an Array of LogData to write to the database.
     */
    public void commitLogData(final LogData data[]) {
        String update = "INSERT INTO " + Constants.logDataTable + " (HOST,INSERTDATE,USERAGENT,REQUESTSTRING,STATUS,CONTENTSIZE) VALUES (?,?,?,?,?,?)";

        jdbcTemplate.batchUpdate(update, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                LogData logdata = data[i];

                ps.setString(1, logdata.getHost());
                ps.setLong(2, logdata.getInsertDate().getTime());
                ps.setString(3, logdata.getUserAgent());
                ps.setString(4, logdata.getRequestString());
                ps.setString(5, logdata.getStatus());
                ps.setString(6, logdata.getContentSize());
            }

            @Override
            public int getBatchSize() {
                return data.length;
            }
        });
    }

    /**
     * Returns a Log Data query in descending order of date.
     * 
     * @param startDate
     *            - The start date to query. This value is required.
     * @param endDate
     *            - The end date to query. This value is required.
     * @param host
     *            - The host to query. The supplied host can be a substring and is not case sensitive. Can be left blank if its not required.
     * @param userAgent
     *            - The userAgent to query. The supplied user agent can be a substring and is not case sensitive. Can be left blank if its not required.
     * @param requestString
     *            - The request String to query. The supplied request string can be a substring and is not case sensitive. Can be left blank if its not required.
     * @param status
     *            - An integer with the desired status. The supplied status must exactly match the status in the database. Can be left blank if its not required.
     * @param contentSize
     *            - An integer with the desired contentSize. The supplied contentSize must exactly match the status in the database. Can be left blank if its not required.
     * @param maxResults
     *            - The maximum amounts of results to return.
     * @return an array of LogData results.
     * @throws SQLException
     */
    public LogData[] queryLogData(Timestamp startDate, Timestamp endDate, String host, String userAgent, String requestString, String status, String contentSize, String maxResults)
            throws SQLException {
        log.trace("Entering queryLogData");
        log.trace("startDate " + startDate.toString());
        log.trace("endDate " + endDate.toString());
        log.trace("host " + host);
        log.trace("userAgent " + userAgent);
        log.trace("requestString " + requestString);
        log.trace("status " + status);
        log.trace("contentSize " + contentSize);
        log.trace("maxResults " + maxResults);
        StringBuffer query = new StringBuffer();

        query.append("SELECT * FROM " + Constants.logDataTable + " WHERE INSERTDATE > " + startDate.getTime() + " AND INSERTDATE < " + endDate.getTime());
        if (!host.equals("")) {
            query.append(" AND UPPER(HOST) LIKE '%" + host.toUpperCase() + "%'");
        }

        if (!userAgent.equals("")) {
            query.append(" AND UPPER(USERAGENT) LIKE '%" + userAgent.toUpperCase().replaceAll("%", "@%").replaceAll("_", "@_") + "%'  {escape '@'}");
        }

        if (!requestString.equals("")) {
            query.append(" AND UPPER(REQUESTSTRING) LIKE '%" + requestString.toUpperCase().replaceAll("%", "@%").replaceAll("_", "@_") + "%'  {escape '@'}");
        }

        if (!status.equals("")) {
            query.append(" AND STATUS='" + status + "'");
        }

        if (!contentSize.equals("")) {
            query.append(" AND CONTENTSIZE='" + contentSize + "'");
        }

        query.append(" ORDER BY INSERTDATE DESC LIMIT " + maxResults);
        log.trace("Query: " + query.toString());

        List<LogData> logData = this.jdbcTemplate.query(query.toString(), new RowMapper<LogData>() {
            public LogData mapRow(ResultSet res, int rowNum) throws SQLException {
                try {
                    Timestamp insertDateResult = new Timestamp((res.getLong("INSERTDATE") * 1000));
                    log.trace("INSERTDATE "+insertDateResult.toString());
                    String hostResult = res.getString("HOST");
                    log.trace("HOST "+hostResult);
                    String userAgentResult = res.getString("USERAGENT");
                    log.trace("USERAGENT "+userAgentResult);
                    String requestStringResult = res.getString("REQUESTSTRING");
                    log.trace("REQUESTSTRING "+requestStringResult);
                    String statusResult = res.getString("STATUS");
                    log.trace("STATUS "+statusResult);
                    String contentSizeResult = res.getString("CONTENTSIZE");
                    log.trace("CONTENTSIZE "+contentSizeResult);

                    return new LogData(insertDateResult, hostResult, userAgentResult, requestStringResult, statusResult, contentSizeResult);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                return null;
            }
        });

        return logData.toArray(new LogData[logData.size()]);
    }

    /**
     * 
     * @param startDate
     *            - The start date to query. This value is required.
     * @param endDate
     *            - The end date to query. This value is required.
     * @param host
     *            - The host to query. The supplied host can be a substring and is not case sensitive. Can be left blank if its not required.
     * @param userAgent
     *            - The userAgent to query. The supplied user agent can be a substring and is not case sensitive. Can be left blank if its not required.
     * @param requestString
     *            - The request String to query. The supplied request string can be a substring and is not case sensitive. Can be left blank if its not required.
     * @param status
     *            - An integer with the desired status. The supplied status must exactly match the status in the database. Can be left blank if its not required.
     * @param contentSize
     *            - An integer with the desired contentSize. The supplied contentSize must exactly match the status in the database. Can be left blank if its not required.
     */
    public void deleteLogData(Timestamp startDate, Timestamp endDate, String host, String userAgent, String requestString, String status, String contentSize) {
        log.trace("Entering deleteLogData");
        log.trace("startDate " + startDate.toString());
        log.trace("endDate " + endDate.toString());
        log.trace("host " + host);
        log.trace("userAgent " + userAgent);
        log.trace("requestString " + requestString);
        log.trace("status " + status);
        log.trace("contentSize " + contentSize);
        StringBuffer update = new StringBuffer();
        update.append("DELETE FROM " + Constants.logDataTable + " WHERE INSERTDATE > " + startDate.getTime() + " AND INSERTDATE < " + endDate.getTime());

        if (!host.equals("")) {
            update.append(" AND UPPER(HOST) LIKE '%" + host.toUpperCase() + "%'");
        }

        if (!userAgent.equals("")) {
            update.append(" AND UPPER(USERAGENT) LIKE '%" + userAgent.toUpperCase().replaceAll("%", "@%").replaceAll("_", "@_") + "%'  {escape '@'}");
        }

        if (!requestString.equals("")) {
            update.append(" AND UPPER(REQUESTSTRING) LIKE '%" + requestString.toUpperCase().replaceAll("%", "@%").replaceAll("_", "@_") + "%'  {escape '@'}");
        }

        if (!status.equals("")) {
            update.append(" AND STATUS='" + status + "'");
        }

        if (!contentSize.equals("")) {
            update.append(" AND CONTENTSIZE='" + contentSize + "'");
        }

        log.trace("Update: " + update.toString());

        this.jdbcTemplate.update(update.toString());

    }

    /**
     * 
     * @param numberOfDays
     *            - Any data older than the supplied number of days will be deleted.
     */
    public void shrinkLogData(int numberOfDays) throws Exception {
        log.trace("Entering shrinkLogData with numberOf Days " + numberOfDays);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, ((-1) * numberOfDays));
        Timestamp date = new Timestamp(cal.getTimeInMillis());

        String update = "DELETE FROM " + Constants.logDataTable + " WHERE INSERTDATE < " + date.getTime();
        log.trace("Update: " + update);

        this.jdbcTemplate.update(update);
    }

    /**
     * 
     * @return the number of log entries in the database.
     */
    public int getNumberOfEntries() {
        log.trace("Entering getNumberOfEntries");

        String query = "SELECT COUNT(INSERTDATE) AS NUMROWS FROM " + Constants.logDataTable;
        int value = this.jdbcTemplate.queryForObject(query, Integer.class);

        if (value == 0) {
            log.trace("There were no entries found in the table");
        }

        return value;
    }

    /**
     * 
     * @return the oldest time in the database.
     */
    public Timestamp getOldestTime() throws Exception {
        log.trace("Entering getOldestTime");

        String query = "SELECT MIN(INSERTDATE) AS MINTIME FROM " + Constants.logDataTable;
        Long value = this.jdbcTemplate.queryForObject(query, Long.class);

        Timestamp time = null;
        if (value != null) {
            time = new Timestamp(value * 1000);
            log.trace("Minimum Time " + value.toString());
        } else {
            log.trace("There were no entries found in the database");
        }

        return time;
    }

    /**
     * 
     * @return the newest time in the database
     */
    public Timestamp getNewestTime() throws Exception {
        log.trace("Entering getNewestTime");

        String query = "SELECT MAX(INSERTDATE) AS MAXTIME FROM " + Constants.logDataTable;
        Long value = this.jdbcTemplate.queryForObject(query, Long.class);

        Timestamp time = null;
        if (value != null) {
            time = new Timestamp(value * 1000);
            log.trace("Maximum Time " + value.toString());
        } else {
            log.trace("There were no entries found in the database");
        }

        return time;
    }

    /**
     * Gets the total amount of transactions per hour over a day.
     * 
     * @param date
     *            - The date to query. Only the day month and year are used.
     * @param host
     *            - The host to query. The supplied host can be a substring and is not case sensitive. Can be left blank if its not required.
     * @param userAgent
     *            - The userAgent to query. The supplied user agent can be a substring and is not case sensitive. Can be left blank if its not required.
     * @param requestString
     *            - The request String to query. The supplied request string can be a substring and is not case sensitive. Can be left blank if its not required.
     * @param status
     *            - An integer with the desired status. The supplied status must exactly match the status in the database. Can be left blank if its not required.
     * @param contentSize
     *            - An integer with the desired contentSize. The supplied contentSize must exactly match the status in the database. Can be left blank if its not required.
     * @return an array with length 24 which has the amount of entries per hour.
     */
    public int[] getDailyReportByHour(Timestamp date, String host, String userAgent, String requestString, String status, String contentSize) throws Exception {
        // select h, count(*) as NUM from (select hour(INSERTDATE) from LOGDATA WHERE INSERTDATE < TIMESTAMP('2011-12-23 00:00:00.0') AND INSERTDATE > TIMESTAMP('2011-12-22 00:00:00.0')) as t(h) group
        // by h;
        log.trace("Entering getDailyReportByHour");
        log.trace("date " + date.toString());
        log.trace("host " + host);
        log.trace("userAgent " + userAgent);
        log.trace("requestString " + requestString);
        log.trace("status " + status);
        log.trace("contentSize " + contentSize);

        Timestamp currentTime = date;
        Calendar tempPresent = Calendar.getInstance();
        tempPresent.setTimeInMillis(currentTime.getTime() * 1000);
        tempPresent.set(Calendar.HOUR_OF_DAY, 0);
        tempPresent.set(Calendar.MINUTE, 0);
        tempPresent.set(Calendar.SECOND, 0);
        tempPresent.set(Calendar.MILLISECOND, 0);
        currentTime.setTime(tempPresent.getTimeInMillis());
        tempPresent.add(Calendar.DAY_OF_YEAR, 1);
        Timestamp futureTime = new Timestamp(tempPresent.getTimeInMillis());

        StringBuffer query = new StringBuffer();

        query.append("SELECT h, count(*) as NUM from (SELECT strftime('%H',datetime(INSERTDATE, 'unixepoch', 'localtime')) as h FROM " + Constants.logDataTable + " WHERE INSERTDATE < " + futureTime.getTime()
                + " AND INSERTDATE > " + currentTime.getTime());
        if (!host.equals("")) {
            query.append(" AND UPPER(HOST) LIKE '%" + host.toUpperCase() + "%'");
        }

        if (!userAgent.equals("")) {
            query.append(" AND UPPER(USERAGENT) LIKE '%" + userAgent.toUpperCase().replaceAll("%", "@%").replaceAll("_", "@_") + "%'  {escape '@'}");
        }

        if (!requestString.equals("")) {
            query.append(" AND UPPER(REQUESTSTRING) LIKE '%" + requestString.toUpperCase().replaceAll("%", "@%").replaceAll("_", "@_") + "%'  {escape '@'}");
        }

        if (!status.equals("")) {
            query.append(" AND STATUS='" + status + "'");
        }

        if (!contentSize.equals("")) {
            query.append(" AND CONTENTSIZE='" + contentSize + "'");
        }

        query.append(") group by h");
        log.trace("Query: " + query.toString());
        List<HourCount> hourCounts = this.jdbcTemplate.query(query.toString(), new RowMapper<HourCount>() {
            public HourCount mapRow(ResultSet res, int rowNum) throws SQLException {
                int hour = res.getInt("h");
                int count = res.getInt("NUM");
                return new HourCount(hour, count);
            }
        });

        int hourCount[] = new int[24];
        for (int i = 0; i < hourCount.length; i++) {
            hourCount[i] = 0;
        }

        for (HourCount curr : hourCounts) {
            hourCount[curr.hour] = curr.count;
        }

        return hourCount;
    }

    private class HourCount {
        private int hour;
        private int count;

        public HourCount(int hour, int count) {
            this.hour = hour;
            this.count = count;
        }
    }

    /**
     * Gets the total amount of transactions per day per month. The 0 position in the returned array should not be used.
     * 
     * @param date
     *            - The date to query. Only the month and year are used.
     * @param host
     *            - The host to query. The supplied host can be a substring and is not case sensitive. Can be left blank if its not required.
     * @param userAgent
     *            - The userAgent to query. The supplied user agent can be a substring and is not case sensitive. Can be left blank if its not required.
     * @param requestString
     *            - The request String to query. The supplied request string can be a substring and is not case sensitive. Can be left blank if its not required.
     * @param status
     *            - An integer with the desired status. The supplied status must exactly match the status in the database. Can be left blank if its not required.
     * @param contentSize
     *            - An integer with the desired contentSize. The supplied contentSize must exactly match the status in the database. Can be left blank if its not required.
     * @return an array with length that matches the amount of the days in the month +1. Each position has the amount of entries per day. The days start at 1 so position 0 is not used.
     */
    public int[] getMonthlyReportByDay(Timestamp date, String host, String userAgent, String requestString, String status, String contentSize) throws Exception {
        log.trace("Entering getDailyReportByHour");
        log.trace("date " + date.toString());
        log.trace("host " + host);
        log.trace("userAgent " + userAgent);
        log.trace("requestString " + requestString);
        log.trace("status " + status);
        log.trace("contentSize " + contentSize);

        Timestamp currentTime = date;
        Calendar tempPresent = Calendar.getInstance();
        tempPresent.setTimeInMillis(currentTime.getTime() * 1000);
        tempPresent.set(Calendar.DAY_OF_MONTH, 1);
        tempPresent.set(Calendar.HOUR_OF_DAY, 0);
        tempPresent.set(Calendar.MINUTE, 0);
        tempPresent.set(Calendar.SECOND, 0);
        tempPresent.set(Calendar.MILLISECOND, 0);
        currentTime.setTime(tempPresent.getTimeInMillis());
        int numOfdaysInMonth = tempPresent.getActualMaximum(Calendar.DAY_OF_MONTH);
        tempPresent.add(Calendar.MONTH, 1);
        Timestamp futureTime = new Timestamp(tempPresent.getTimeInMillis());

        StringBuffer query = new StringBuffer();

        query.append("SELECT d, count(*) as NUM from (SELECT strftime('%d',datetime(INSERTDATE, 'unixepoch', 'localtime')) as d FROM " + Constants.logDataTable + " WHERE INSERTDATE < " + futureTime.getTime()
                + " AND INSERTDATE > " + currentTime.getTime());
        if (!host.equals("")) {
            query.append(" AND UPPER(HOST) LIKE '%" + host.toUpperCase() + "%'");
        }

        if (!userAgent.equals("")) {
            query.append(" AND UPPER(USERAGENT) LIKE '%" + userAgent.toUpperCase().replaceAll("%", "@%").replaceAll("_", "@_") + "%'  {escape '@'}");
        }

        if (!requestString.equals("")) {
            query.append(" AND UPPER(REQUESTSTRING) LIKE '%" + requestString.toUpperCase().replaceAll("%", "@%").replaceAll("_", "@_") + "%'  {escape '@'}");
        }

        if (!status.equals("")) {
            query.append(" AND STATUS='" + status + "'");
        }

        if (!contentSize.equals("")) {
            query.append(" AND CONTENTSIZE='" + contentSize + "'");
        }

        query.append(") group by d");
        log.trace("Query: " + query.toString());

        List<DayCount> dayCounts = this.jdbcTemplate.query(query.toString(), new RowMapper<DayCount>() {
            public DayCount mapRow(ResultSet res, int rowNum) throws SQLException {
                int day = res.getInt("d");
                int count = res.getInt("NUM");
                return new DayCount(day, count);
            }
        });

        // Add an additional day for the unused day position
        int dayCount[] = new int[numOfdaysInMonth + 1];
        for (int i = 0; i < dayCount.length; i++) {
            dayCount[i] = 0;
        }

        for (DayCount curr : dayCounts) {
            dayCount[curr.day] = curr.count;
        }

        return dayCount;
    }

    private class DayCount {
        private int day;
        private int count;

        public DayCount(int day, int count) {
            this.day = day;
            this.count = count;
        }
    }
}
