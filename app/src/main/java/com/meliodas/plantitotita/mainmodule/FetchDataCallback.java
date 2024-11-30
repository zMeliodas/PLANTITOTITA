package com.meliodas.plantitotita.mainmodule;

import java.util.List;
import java.util.Map;

public interface FetchDataCallback {
    void onFetchComplete(List<Map<String, Object>> data);
}