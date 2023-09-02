package com.example.redis.search;

import com.example.redis.search.model.SearchVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@Component
public class SearchService {

    private final RedisTemplate<String, String> redisTemplate;
    private final  TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledTask;
    private final Long keysize = 5L;

    public Double search(String product){

        Double babymeal = null;
        try{
            babymeal = redisTemplate.opsForZSet().incrementScore("babymeal", product, 1);
            //redisTemplate.opsForZSet().incrementScore("ranking", keyword, 1, TimeUnit.MILLISECONDS);
        }catch (Exception e) {
            System.out.println(e.toString());
        }

        Runnable task = () -> {
             redisTemplate.opsForZSet().incrementScore("babymeal", product, -1);
            System.out.println(product);
        };
        taskScheduler.schedule(task, Date.from(Instant.now().plus(30, ChronoUnit.SECONDS)));

        //scheduledTask = taskScheduler.schedule(this::executeTask, Date.from(Instant.now().plus(24, ChronoUnit.HOURS)));
        return babymeal;
    }


    public List<SearchVo>list(){
        String key = "babymeal";
        ZSetOperations<String, String> ZSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> typedTuples = ZSetOperations.reverseRangeWithScores(key, 0, 9);  //score순으로 10개 보여줌
        return typedTuples.stream().map(item-> SearchVo.builder().product(item.getValue()).count(item.getScore()).build()).toList();
         //typedTuples.stream().map(SearchRankResponseDto::convertToResponseRankingDto).collect(Collectors.toList());
    }

    public List<String> recentSearch(String product) {
        Long user = 1L;
        String key = String.valueOf(user);

        //레디스에 중복된 단어를 저장 하지 못하도록 하자

        List<String> check = redisTemplate.opsForList().range(key, 0, keysize);

        for (int i = 0; i <check.size(); i++) {
            String redisproduct = check.get(i);
            if (redisproduct.equals(product)){
                return check;
            }
        }

        //레디스에 5개 이상 저장 하지 못하도록 하자
        Long size = redisTemplate.opsForList().size(key);
        if (size == keysize) {
            redisTemplate.opsForList().rightPop(key);
        }


        Long result = redisTemplate.opsForList().leftPush(key, product);
        log.info("result:{}",result);

        //검색
        List<String> list = redisTemplate.opsForList().range(key, 0, keysize);
        return list;

    }

    public List<String> GetRecentSearch() {
        Long user = 1L;
        String key = String.valueOf(user);
        int start = 0;
        List<String> range = redisTemplate.opsForList().range(key, start, keysize);

        return range;
    }
    public Long deleteRecentSearch(String product){
        Long user = 1L;
        String key = String.valueOf(user);
        Long remove = redisTemplate.opsForList().remove(key, 0, product);
        return remove;

    }
}
