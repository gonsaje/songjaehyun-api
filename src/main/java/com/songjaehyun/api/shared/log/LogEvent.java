package com.songjaehyun.api.shared.log;

import java.util.Map;

public record LogEvent(
    long tsMillis,
    String demo,
    String method,
    Map<String,Object> args,
    Object result
) {}
