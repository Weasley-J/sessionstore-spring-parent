package io.github.weasleyj.sessionstore.support;

import com.fasterxml.jackson.core.type.TypeReference;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;

/**
 * Redisson Client Codec Support
 *
 * @author weasley
 * @version 1.0.0
 * @see TypedJsonJacksonCodec
 * @see Codec
 */
public final class CodecSupport {
    /**
     * Returns a Codec
     *
     * @param <T> Value Type Reference
     * @return TypedJsonJacksonCodec
     */
    public static <T> TypedJsonJacksonCodec codec(Class<T> valueClass) {
        return new TypedJsonJacksonCodec(valueClass);
    }

    /**
     * Returns a Codec
     *
     * @param <T> Value Type Reference
     * @return TypedJsonJacksonCodec
     */
    public static <T> TypedJsonJacksonCodec codec(TypeReference<T> valueTypeReference) {
        return new TypedJsonJacksonCodec(valueTypeReference);
    }
}
