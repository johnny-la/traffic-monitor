package traffic.monitor; 

/**
 * Informs subscribers when a monitor triggers an alert
 */
interface AlertListener
{
    /** 
     * Called when an alert is triggered
     * @param alert The alert which was triggered
     */
    void alertTriggered(Alert alert);
}