package unn.dataset;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import unn.dataset.datacenter.DatacenterOrigin;
import unn.structures.Agent;

import java.util.HashMap;
import java.util.List;

public interface MaestroService {
    @GET("/datacenter/origin")
    Call<DatacenterOrigin> findDatacenter();

    @POST("/agent/register")
    Call<> registerAgent(Agent agent);
}
