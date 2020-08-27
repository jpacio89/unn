package unn.structures;

public enum StatusResponse {
    SUCCESS ("Success"),
    ERROR ("Error");

    private String status;

    StatusResponse(String _status) {
        this.status = _status;
    }
}
