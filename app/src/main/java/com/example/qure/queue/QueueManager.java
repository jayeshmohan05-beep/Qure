package com.example.qure.queue;

import com.example.qure.models.Appointment;
import com.example.qure.utils.Constants;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * QueueManager.java — Priority + FCFS Queue Scheduling
 * -------------------------------------------------------
 * WHAT THIS FILE DOES:
 *   Manages the waiting queue for doctors.
 *
 *   Algorithm: FCFS (First-Come, First-Served) with Emergency Preemption.
 *   - Normal patients are served strictly in order of arrival (FCFS).
 *   - Any patient flagged by the Admin as an Emergency immediately jumps
 *     to the front of the queue to be the next patient consulted.
 *   - Multiple emergency patients are ordered FCFS among themselves.
 * -------------------------------------------------------
 */
public class QueueManager {

    public static List<Appointment> sortQueue(List<Appointment> queue) {
        if (queue == null || queue.isEmpty()) {
            return queue;
        }

        // FCFS with Emergency Preemption logic
        Collections.sort(queue, new Comparator<Appointment>() {
            @Override
            public int compare(Appointment a1, Appointment a2) {
                boolean e1 = isEmergency(a1);
                boolean e2 = isEmergency(a2);

                // 1. Emergency Preemption: Emergency patients go to the front
                if (e1 && !e2) {
                    return -1; // a1 is emergency, comes first
                }
                if (!e1 && e2) {
                    return 1;  // a2 is emergency, comes first
                }

                // 2. Both are Emergency OR both are Regular: FCFS (First-Come, First-Served)
                long arr1 = parseArrivalTime(a1.getArrivalTime());
                long arr2 = parseArrivalTime(a2.getArrivalTime());

                if (arr1 != arr2 && arr1 != Long.MAX_VALUE && arr2 != Long.MAX_VALUE) {
                    return Long.compare(arr1, arr2);
                }

                // If arrival times are equal or not recorded, sort by token number
                return Integer.compare(a1.getTokenNumber(), a2.getTokenNumber());
            }
        });

        return queue;
    }

    /**
     * Checks if an appointment is flagged as an Emergency.
     */
    public static boolean isEmergency(Appointment a) {
        if (a == null) return false;
        return a.getPriority() == Constants.PRIORITY_EMERGENCY ||
                Constants.STATUS_EMERGENCY.equalsIgnoreCase(a.getStatus());
    }

    /**
     * Helper method to safely parse the arrival time string (epoch millis).
     */
    private static long parseArrivalTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return Long.MAX_VALUE;
        }
        try {
            return Long.parseLong(timeStr);
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }
}
