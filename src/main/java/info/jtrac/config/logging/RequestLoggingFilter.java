package info.jtrac.config.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    public static final String CSRF_TOKEN_ATTR_NAME = HttpSessionCsrfTokenRepository.class.getName().concat(".CSRF_TOKEN");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Wrap the request to make the body readable multiple times
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(req);

        // Proceed with the filter chain first, so that the request body is read
        chain.doFilter(wrappedRequest, response);

        logger.info("Incoming request: {} {}", wrappedRequest.getMethod(), wrappedRequest.getRequestURI());

        if ("POST".equalsIgnoreCase(wrappedRequest.getMethod())) {
            logRequestBody(wrappedRequest);
        }

        logger.info("Outgoing response for {} {}: {}", wrappedRequest.getMethod(), wrappedRequest.getRequestURI(), res.getStatus());
    }

    private void logRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                String body = new String(buf, 0, buf.length, request.getCharacterEncoding());
                logger.info("Request body: {}", body);

                // Manually parse the body to get the CSRF token
                Map<String, String> params = parseUrlEncodedBody(body);
                String csrfFromRequest = params.get("_csrf");
                if (csrfFromRequest != null) {
                    logger.info("CSRF token from request body: {}", csrfFromRequest);
                } else {
                    logger.warn("CSRF token not found in request body.");
                }


            } catch (UnsupportedEncodingException e) {
                logger.warn("Could not parse request body", e);
            }
        }
    }

    private Map<String, String> parseUrlEncodedBody(String body) {
        return Arrays.stream(body.split("&"))
                .map(param -> param.split("="))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                        parts -> decode(parts[0]),
                        parts -> decode(parts[1]),
                        (first, second) -> second // In case of duplicate keys, keep the last one
                ));
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // Should not happen with UTF-8
            return "";
        }
    }
}
