local key = KEYS[1] -- Redis Key
local noteId = ARGV[1] -- 笔记ID
local timestamp = ARGV[2] -- 时间戳
--local expireTime = ARGV[3]


-- 获取笔记收藏列表大小
local size = redis.call('ZCARD', key)

-- 若已经收藏了 300 篇笔记，则移除最早收藏的那篇
if size >= 300 then
    redis.call('ZPOPMIN', key)
end

-- 添加新的笔记收藏关系
redis.call('ZADD', key, timestamp, noteId)

local baseExpire = 60 * 60 * 24               -- 86400 秒
local randomExtra = math.random(0, 60 * 60 * 24)  -- 0 ~ 86399 秒
local totalExpire = baseExpire + randomExtra
redis.call('EXPIRE', key, totalExpire)
--redis.call('EXPIRE', key, expireTime)
return 0
