-- 获取 key, 限制数, 时间窗口
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])

-- 当前已请求次数
local current = tonumber(redis.call('get', key) or "0")

if current + 1 > limit then
    return 0 -- 拒绝请求
else
    -- 次数自增
    redis.call("INCRBY", key, 1)
    if current == 0 then
        -- 第一次请求时设置过期时间
        redis.call("EXPIRE", key, window)
    end
    return 1 -- 准予通过
end
