package keeper.project.homepage.dto;

import java.sql.Date;
import keeper.project.homepage.entity.library.BookBorrowEntity;
import keeper.project.homepage.entity.library.BookEntity;
import keeper.project.homepage.entity.member.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookBorrowDto {

  private Long memberId;
  private Long bookId;
  private Long quantity;
  private Date borrowDate;
  private Date expireDate;

  public BookBorrowEntity toEntity(BookEntity bookEntity, MemberEntity memberEntity) {

    return BookBorrowEntity.builder().memberId(memberEntity).bookId(bookEntity).quantity(quantity)
        .borrowDate(borrowDate).expireDate(expireDate).build();
  }


}
