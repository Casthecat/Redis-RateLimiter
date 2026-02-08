-- Get key, limit, time window
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])

-- Current request count
local current = tonumber(redis.call('get', key) or "0")

if current + 1 > limit then
    return 0 -- Reject request
else
    -- Increment count
    redis.call("INCRBY", key, 1)
    if current == 0 then
        -- Set expiry on first request
        redis.call("EXPIRE", key, window)
    end
    return 1 -- Allow request
end
