package com.example.redis.search;

import com.example.redis.search.model.SearchVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

@Slf4j
@Service
@RequiredArgsConstructor
@Component
public class SearchService {

    private final RedisTemplate<String, String> redisTemplate;
    private final  TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledTask;

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




//    private final AtomicBoolean hasRun = new AtomicBoolean(false);
//
//    //@Scheduled(fixedDelay = 24 * 60 * 60 * 1000, initialDelay = 24 * 60 * 60 * 1000)
//    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
//    public void run() {
//        if (hasRun.compareAndSet(false, true)) {
//            // 여기에 실행될 로직 작성
//            System.out.println("product");
//        }
//    }



    public List<SearchVo>list(){
        String key = "babymeal";
        ZSetOperations<String, String> ZSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> typedTuples = ZSetOperations.reverseRangeWithScores(key, 0, 9);  //score순으로 10개 보여줌
        return typedTuples.stream().map(item-> SearchVo.builder().product(item.getValue()).count(item.getScore()).build()).toList();
         //typedTuples.stream().map(SearchRankResponseDto::convertToResponseRankingDto).collect(Collectors.toList());
    }
}
