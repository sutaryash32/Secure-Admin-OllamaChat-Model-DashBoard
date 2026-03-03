    package com.ai.demo.RestControllers;

    import com.ai.demo.Service.ChatService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.web.bind.annotation.*;
    import reactor.core.publisher.Flux;

    import java.time.Duration;
    import java.util.Map;

    @RestController
    @RequestMapping("/api/v1/chat")
    public class ChatController {

        @Autowired
        private ChatService cs;

        // Public endpoint for testing
        @GetMapping("/health")
        public ResponseEntity<Map<String, String>> health() {
            return ResponseEntity.ok(Map.of("status", "UP"));
        }



    //    @GetMapping
    //    public ResponseEntity<Map<String, String>> generateResponse(
    //            @RequestParam String prompt) {
    //        String respText = cs.generateResponse(prompt);
    //        return ResponseEntity.ok(Map.of("content", respText));
    //    }


        @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<Map<String, String>> generateResponse(@RequestParam String prompt) {
            return cs.generateResponseReactive(prompt)
                    .map(resp -> Map.of("content", resp))
                    .concatWith(Flux.just(Map.of("content", "[DONE]"))); // ✅ completion signal
        }


        @GetMapping(value = "/cricket", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<Map<String, String>> generateCricketResponse(@RequestParam String prompt) {
            return cs.generateCricketResponseReactive(prompt)
                    .map(resp -> Map.of("content", resp));
        }





    }
