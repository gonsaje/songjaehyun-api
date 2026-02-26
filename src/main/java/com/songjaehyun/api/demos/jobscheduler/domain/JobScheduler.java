package com.songjaehyun.api.demos.jobscheduler.domain;

import java.util.List;

/*
    Design Constraints:
        All operations should aim for O(log n) or better
        No background threads
        Cooldown enforcement is lazy (checked on access)
        Must be thread-safe (use locks if needed)

    Hidden Edge Cases (you must handle):
        Cooldown = 0
        Multiple jobs with same priority
        Job executed multiple times
        Job updated while cooling down
        topK(k) where k > available jobs
        All jobs cooling down
*/

public class JobScheduler {
    /*
     * jobId is unique.
     * priority = higher number means more important.
     * cooldownMillis = minimum time that must pass between executions.
     * If job already exists → overwrite its config.
     */
    public void registerJob(String jobId, int priority, long cooldownMillis) {

    }

    /*
     * This should:
     * Return the jobId of the highest-priority job
     * BUT only if it is eligible (cooldown has passed)
     * If no jobs are eligible → return null
     * When a job executes:
     * Record its last execution time
     * Enforce cooldown before it can run again
     */
    public String executeNext() {
        return "";
    }

    /*
     * Reduce its priority by 1 (minimum 0)
     * Failed jobs should still respect cooldown
     */
    void markFailed(String jobId) {

    }

    /*
     * Return the top K eligible jobs ordered by:
     * Priority (desc)
     * Earliest last execution time
     * Lexicographically by jobId
     * ⚠️ This method is your algorithmic method.
     * You may NOT:
     * Sort the entire collection every time (that’s lazy)
     * Scan repeatedly in inefficient ways
     * You need a clean data structure approach.
     */
    public List<String> topK(int k) {
        return List.of("");
    }

    long getRemainingCooldown(String jobId) {
        return 10000000;
    }

    void removeJob(String jobId) {

    }
}
