package unn.structures;

public class StandardResponse {
    private StatusResponse status;
    private String message;
    // private JsonElement data;
    private Object data;

    public StandardResponse(StatusResponse _status) {
        this.status = _status;
    }

    public StandardResponse(StatusResponse _status, String _message, Object _data) {
        this.status = _status;
        this.message = _message;
        this.data = _data;
    }

    public StatusResponse getStatus() {
        return status;
    }

    public void setStatus(StatusResponse status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
