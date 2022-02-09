package keeper.project.homepage.controller.library;

import java.util.List;
import keeper.project.homepage.entity.library.BookEntity;
import keeper.project.homepage.entity.posting.PostingEntity;
import keeper.project.homepage.service.library.LibraryMainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
@Log4j2
public class LibraryMainController {

  private final LibraryMainService libraryMainService;

  @GetMapping(value = "/recentbooks")
  public List<BookEntity> displayRecentBooks() {

    return libraryMainService.displayTenBooks();
  }

  @GetMapping(value = "/searchbooks")
  public ResponseEntity<List<BookEntity>> searchBooks(@RequestParam String keyword,
      @PageableDefault(size = 10, sort = "id", direction = Direction.ASC) Pageable pageable) {

    return ResponseEntity.status(HttpStatus.OK).body(libraryMainService.searchBooks(keyword, pageable));
  }

}
