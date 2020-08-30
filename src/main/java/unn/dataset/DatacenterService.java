package unn.dataset;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DatacenterService {
    @GET("/dataset/{namespace}/agent/miner/body")
    Call<String> fetchDataset(@Path("namespace") String namespace);

    @GET("/dataset/features/random/layer/{layer}")
    Call<String> getRandomFeatures(@Path("layer") int layer);

    @GET("/dataset/register")
    Call<String> registerAgent(@Body DatasetDescriptor descriptor);

}
