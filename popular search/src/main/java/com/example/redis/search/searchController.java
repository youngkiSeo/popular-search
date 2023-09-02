package com.example.redis.search;

import com.example.redis.search.model.SearchVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class searchController {
    private final SearchService service;

    @PostMapping("/popualr")
    public Double searchRankList(@RequestParam String product){
        return service.search(product);
    }

    @GetMapping("/popualr")
    public List<SearchVo> searchRankList(){
        return service.list();
    }

    @PostMapping("recent")
    public List<String> recentSearch(String product){
        return service.recentSearch(product);
    }
    @DeleteMapping("recent")
    public Long removeRecent(String product){
        return service.deleteRecentSearch(product);
    }

}
