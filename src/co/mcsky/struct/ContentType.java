package co.mcsky.struct;

import java.util.Optional;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * See <a href="https://www.rfc-editor.org/rfc/rfc1945.html#section-10.5">RFC
 * 1945 10.5</a>.
 */
public enum ContentType {
    TEXT,
    IMAGE;

    /**
     * @param spec the string representation of value of {@code Content-Type}
     *
     * @return the media-type of this string representation if there is one
     * existing, otherwise returns {@code null}
     */
    public static ContentType matchType(String spec) {
        return ofNullable(spec)
                .flatMap(s -> {
                    if (s.startsWith("text")) {
                        return of(ContentType.TEXT);
                    } else if (s.startsWith("image")) {
                        return of(ContentType.IMAGE);
                    }
                    return Optional.empty();
                })
                .orElse(null);
    }
}
