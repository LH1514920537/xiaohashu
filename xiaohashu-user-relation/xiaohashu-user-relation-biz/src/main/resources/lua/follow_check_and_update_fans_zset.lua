local key = KEYS[1] -- 操作的 Redis Key
local fansUserId = ARGV[1] -- 粉丝ID
local timestamp = ARGV[2] -- 时间戳

-- 初始化 size 为 0
local size = 0
if redis.call('EXISTS', key) == 1 then
    size = redis.call('ZCARD', key)
end

-- 若超过 5000 个粉丝，则移除最早关注的粉丝
if size >= 5000 then
    redis.call('ZPOPMIN', key)
end

-- 添加新的粉丝关系(新的粉丝关系或添加新的列表)
redis.call('ZADD', key, timestamp, fansUserId)
return 0
