
public class StatusCodes {

    int code;
    String reason;

    public StatusCodes(int code, String reason) {
        this.code = code;
        this.reason = reason;
        getStatusCode();
    }

    public void getStatusCode() {
        switch (this.code) {
            case 200:
                System.out.println(this.code + " OK " + this.reason);
                break;
            case 404:
                System.out.println(this.code + " Not Found " + this.reason);
                break;
            case 500:
                System.out.println(this.code + " Server Error " + this.reason);
                break;
            case 304:
                System.out.println(this.code + " Not Modified " + this.reason);
                break;
        }
    }

}