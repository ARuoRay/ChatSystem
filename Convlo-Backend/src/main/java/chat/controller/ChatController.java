package chat.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import chat.model.dto.ChatDto;
import chat.model.dto.ChatroomDto;
import chat.model.dto.Profile;
import chat.model.dto.UserDto;
import chat.model.entity.Chat;
import chat.model.entity.Message;
import chat.model.entity.User;
import chat.model.request.AddUserRequest;
import chat.model.request.DeleteChatRequest;
import chat.model.request.LeaveChatRequest;
import chat.repository.UserRepository;
import chat.response.ApiResponse;
import chat.service.ChatService;
import chat.service.UserService;

@RestController
@RequestMapping("/home/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;
    
    @Autowired
	private UserService userService;
    
    @Autowired
    private UserRepository userRepository;

    // **1. 創建聊天室**
    @PostMapping
    public ResponseEntity<ApiResponse<ChatDto>> createChat(@RequestBody ChatDto chatDto, @AuthenticationPrincipal String username) {
	
    	 User creator = userRepository.findByUsername(username)
    	            .orElseThrow(() -> new RuntimeException("此會員不存在"));
    	
    	//設置創建者
    	 UserDto creatorDto = new UserDto(creator.getUsername(), creator.getNickName(), creator.getGender());
    	
    	 chatDto.setCreator(creatorDto);
    	
    	chatService.createChat(chatDto);

    	return ResponseEntity.ok(ApiResponse.success("創建成功", chatDto));
    }


    
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<ChatDto>>> findAllChatByUser(@AuthenticationPrincipal String username) {
        // 調用服務層，獲取該用戶參與的所有 Chat 實體
        List<ChatDto> chats = chatService.findAllChatByUser(username);

        // 將 Chat 實體轉換為 ChatDto
        List<ChatDto> chatDtos = chats.stream().map(chat -> {
            ChatDto dto = new ChatDto();
            dto.setChatname(chat.getChatname());
            dto.setCreateAt(chat.getCreateAt());
            dto.setCreator(chat.getCreator()); // 使用 Chat 中的 creator 關聯

            return dto;
        }).collect(Collectors.toList());

        // 返回包含 ChatDto 的 API 響應
        return ResponseEntity.ok(ApiResponse.success("獲取聊天室成功", chatDtos));
    }
    

    
    @PostMapping("/addUser")
    public ResponseEntity<ApiResponse<ChatroomDto>> addUserToChat(
            @RequestBody AddUserRequest request,
            @AuthenticationPrincipal String currentUsername) {
        
        // 驗證請求參數是否完整
        if (request.getChatId() == null || request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "請求參數不完整"));
        }

        try {
            // 調用服務層，執行將目標用戶加入聊天室的邏輯
            ChatroomDto updatedChatDto = chatService.addUserToChat(request.getChatId(), request.getUsername());

            // 返回成功響應
            return ResponseEntity.ok(ApiResponse.success("用戶成功加入聊天室", updatedChatDto));

        } catch (RuntimeException e) {
            // 捕獲業務層拋出的異常，並返回錯誤響應
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "加入失敗: " + e.getMessage()));
        }
    }


//		待開發
//    // **4. 獲取聊天室中的所有消息**
//    @GetMapping("/{chatId}/messages")
//    public ResponseEntity<List<Message>> getMessagesByChatId(@PathVariable Long chatId) {
//        try {
//            List<Message> messages = chatService.getMessagesByChatId(chatId);
//            return ResponseEntity.ok(messages);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(null);
//        }
//    }
    
    

    // **5. 用戶退出聊天室**
    @PostMapping("/leave")
    public ResponseEntity<ApiResponse<ChatroomDto>> leaveChat(
            @RequestBody LeaveChatRequest request,
            @AuthenticationPrincipal String username) {

        // 驗證請求參數是否完整
        if (request.getChatId() == null || request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "請求參數不完整"));
        }

        try {
            // 調用服務層，執行用戶離開聊天室的邏輯
            ChatroomDto updatedChatDto = chatService.leaveChat(request.getChatId(), request.getUsername());

            // 返回成功響應
            return ResponseEntity.ok(ApiResponse.success("用戶成功離開聊天室", updatedChatDto));

        } catch (RuntimeException e) {
            // 捕獲業務層拋出的異常，並返回錯誤響應
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "離開失敗: " + e.getMessage()));
        }
    }

    
    
    
    // **6. 刪除聊天室（僅內部調用）**
    // 注意：此方法應該標記為內部使用，外部客戶端不應直接調用
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteChat(
            @RequestBody DeleteChatRequest request,
            @AuthenticationPrincipal String username) {

        // 驗證請求參數是否完整
        if (request.getChatId() == null ) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "請求參數不完整"));
        }

        try {
            // 調用服務層刪除聊天室邏輯
            chatService.deleteChat(request.getChatId());

            // 返回成功響應
            return ResponseEntity.ok(ApiResponse.success("聊天室已成功刪除",null));
        } catch (RuntimeException e) {
            // 捕獲業務層拋出的異常，並返回錯誤響應
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "刪除失敗: " + e.getMessage()));
        }
    }
}
