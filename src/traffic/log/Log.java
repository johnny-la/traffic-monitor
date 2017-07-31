package traffic.log;
/**
 * A container for a log parsed from in Apache common log format.
 */
public class Log
{
    public String host;
    public String id;
    public String authUser;
    public String date;
    public String request;
    public String requestMethod;
    public String requestUrl;
    public String requestProtocol;
    public String status;
    public String bytes;
}
