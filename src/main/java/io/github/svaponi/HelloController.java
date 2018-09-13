package io.github.svaponi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.svaponi.resource.AnyResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Controller
public class HelloController {

    final static String REQUEST_METHOD = "request-method";
    final static String REQUEST_URI = "request-uri";
    final static String REQUEST_QUERY = "request-query";
    final static String HEADERS = "headers";
    final static String COOKIES = "cookies";
    final static String BODY = "body";

    @RequestMapping(value = "/**") // whatever is not defined by other controllers goes here ...
    public HttpEntity<?> root(final HttpServletRequest request, @RequestBody(required = false) final byte[] body) {

        final AnyResource resource = new AnyResource();

        if (body != null) {
            try {
                resource.withField(BODY, new ObjectMapper().readValue(body, Object.class));
            } catch (final IOException e) {
                resource.withField(BODY, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        if (request != null) {
            resource.withField(REQUEST_METHOD, request.getMethod())
                    .withField(REQUEST_URI, request.getRequestURI())
                    .withFieldIfValue(REQUEST_QUERY, request.getQueryString(), Objects::nonNull)
                    .withFieldIfValueNot(HEADERS, toHeadersMap(request), CollectionUtils::isEmpty)
                    .withFieldIfValueNot(COOKIES, toCookiesMap(request), CollectionUtils::isEmpty)
                    .withFieldIfValue("servlet-path", request.getServletPath(), Objects::nonNull)
                    .withFieldIfValue("server-port", request.getServerPort(), Objects::nonNull)
                    .withFieldIfValue("local-port", request.getLocalPort(), Objects::nonNull)
                    .withFieldIfValue("local-addr", request.getLocalAddr(), Objects::nonNull)
                    .withFieldIfValue("local-name", request.getLocalName(), Objects::nonNull)
                    .withFieldIfValue("session-id", request.getRequestedSessionId(), Objects::nonNull)
                    .withFieldIfValue("remote-addr", request.getRemoteAddr(), Objects::nonNull)
                    .withFieldIfValue("remote-user", request.getRemoteUser(), Objects::nonNull)
                    .withFieldIfValue("remote-host", request.getRemoteHost(), Objects::nonNull)
                    .withFieldIfValue("remote-port", request.getRemotePort(), Objects::nonNull);
        }

        logRequest(request, body);

        return ResponseEntity.ok(resource);
    }

    private static void logRequest(final HttpServletRequest request, final byte[] body) {
        try {
            log.info(logRequest(request.getMethod(), request.getRequestURI(), toHeadersMultiValueMap(request), body));
        } catch (final Exception e) {
            log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage(), log.isDebugEnabled() ? e : null);
        }
    }

    private static Map<String, Object> toCookiesMap(final HttpServletRequest request) {
        return request.getCookies() == null ? null : Arrays.stream(request.getCookies()).collect(Collectors.toMap((Cookie c) -> c.getName(), (Cookie c) -> toCookieResource(c)));
    }

    private static AnyResource toCookieResource(final Cookie c) {
        return new AnyResource()
                .withFieldIfValue("value", c.getValue(), Objects::nonNull)
                .withFieldIfValue("comment", c.getComment(), Objects::nonNull)
                .withFieldIfValue("domain", c.getDomain(), Objects::nonNull)
                .withFieldIfValue("max-age", c.getMaxAge(), maxAge -> maxAge >= 0)
                .withFieldIfValue("path", c.getPath(), Objects::nonNull)
                .withFieldIfValue("secure", c.getSecure(), secure -> secure)
                .withFieldIfValue("version", c.getVersion(), version -> version > 0);
    }

    private static Map<String, Object> toHeadersMap(final HttpServletRequest request) {
        final Map map = new TreeMap();
        toHeadersMultiValueMap(request).forEach((key, values) -> {
            if (values == null || values.isEmpty()) {
                map.put(key, null);
            } else if (values.size() == 1) {
                map.put(key, values.get(0));
            } else {
                map.put(key, values);
            }
        });
        return map;
    }

    private static Map<String, List<String>> toHeadersMultiValueMap(final HttpServletRequest request) {
        final MultiValueMap multiValueMap = new LinkedMultiValueMap();
        final Enumeration<String> names = request.getHeaderNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                final String name = names.nextElement();
                final Enumeration<String> values = request.getHeaders(name);
                while (values.hasMoreElements()) {
                    multiValueMap.add(name, values.nextElement());
                }
            }
        }
        return multiValueMap;
    }

    private static String logRequest(final String method, final String url,
                                     final Map<String, List<String>> headers, final byte[] body) {
        final StringBuilder sb = new StringBuilder();
        sb.append(">>> ");
        sb.append(method);
        sb.append(" ");
        sb.append(url);
        if (!CollectionUtils.isEmpty(headers)) {
            sb.append("\n");
            sb.append("\n");
            headers.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .forEach(e -> {
                        final Object value = e.getValue().size() == 1 ? e.getValue().iterator().next() : e.getValue();
                        sb.append(e.getKey());
                        sb.append(": ");
                        sb.append(value);
                        sb.append("\n");
                    });
        }
        if (body != null && body.length > 0) {
            sb.append("\n");
            sb.append(bodyToString(body));
            sb.append("\n");
        }
        return sb.toString();
    }

    private static final int CRITICAL_SIZE = 1000; // 1 KB

    private static String bodyToString(final byte[] body) {
        if (body.length >= CRITICAL_SIZE) {
            return new String(Arrays.copyOfRange(body, 0, CRITICAL_SIZE)) + String.format(" ... (omitted %d bytes)", body.length - CRITICAL_SIZE);
        } else {
            return new String(body);
        }
    }
}


