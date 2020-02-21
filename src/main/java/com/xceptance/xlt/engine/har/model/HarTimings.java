package com.xceptance.xlt.engine.har.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This object describes various phases within request-response round trip. All times are specified in milliseconds.
 *
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/#timings">specification</a>
 */
@JsonPropertyOrder(
    {
        "blocked", "dns", "connect", "send", "wait", "receive", "ssl", "comment"
    })
public class HarTimings
{

    private final Long blocked;

    private final Long dns;

    private final Long connect;

    private final long send;

    private final long wait;

    private final long receive;

    private final Long ssl;

    private final String comment;

    @JsonCreator
    public HarTimings(@JsonProperty("blocked") Long blocked, @JsonProperty("dns") Long dns, @JsonProperty("connect") Long connect,
                      @JsonProperty("send") long send, @JsonProperty("wait") long wait, @JsonProperty("receive") long receive,
                      @JsonProperty("ssl") Long ssl, @JsonProperty("comment") String comment)
    {
        this.blocked = blocked;
        this.dns = dns;
        this.connect = connect;
        this.send = send;
        this.wait = wait;
        this.receive = receive;
        this.ssl = ssl;
        this.comment = comment;
    }

    public long getSend()
    {
        return send;
    }

    public Long getConnect()
    {
        return connect;
    }

    public Long getDns()
    {
        return dns;
    }

    public Long getSsl()
    {
        return ssl;
    }

    public Long getBlocked()
    {
        return blocked;
    }

    public long getWait()
    {
        return wait;
    }

    public String getComment()
    {
        return comment;
    }

    public long getReceive()
    {
        return receive;
    }

    @Override
    public String toString()
    {
        return "HarTimings [send = " + send + ", connect = " + connect + ", dns = " + dns + ", ssl = " + ssl + ", blocked = " + blocked +
               ", wait = " + wait + ", comment = " + comment + ", receive = " + receive + "]";
    }

    public static class Builder
    {
        private Long blocked;

        private Long dns;

        private Long connect;

        private long send;

        private long wait;

        private long receive;

        private Long ssl;

        private String comment;

        public Builder withBlocked(Long blocked)
        {
            this.blocked = blocked;
            return this;
        }

        public Builder withDns(Long dns)
        {
            this.dns = dns;
            return this;
        }

        public Builder withConnect(Long connect)
        {
            this.connect = connect;
            return this;
        }

        public Builder withSend(long send)
        {
            this.send = send;
            return this;
        }

        public Builder withWait(long wait)
        {
            this.wait = wait;
            return this;
        }

        public Builder withReceive(long receive)
        {
            this.receive = receive;
            return this;
        }

        public Builder withSsl(Long ssl)
        {
            this.ssl = ssl;
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public HarTimings build()
        {
            return new HarTimings(blocked, dns, connect, send, wait, receive, ssl, comment);
        }
    }
}
