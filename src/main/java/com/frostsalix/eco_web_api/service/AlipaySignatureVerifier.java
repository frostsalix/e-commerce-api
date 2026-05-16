package com.frostsalix.eco_web_api.service;

import java.util.Map;

public interface AlipaySignatureVerifier {
    boolean verify(Map<String, String> params);
}
