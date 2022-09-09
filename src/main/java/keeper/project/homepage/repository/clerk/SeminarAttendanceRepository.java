package keeper.project.homepage.repository.clerk;

import java.util.List;
import java.util.Optional;
import keeper.project.homepage.entity.clerk.SeminarAttendanceStatusEntity;
import keeper.project.homepage.entity.member.MemberEntity;
import keeper.project.homepage.entity.clerk.SeminarAttendanceEntity;
import keeper.project.homepage.entity.clerk.SeminarEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeminarAttendanceRepository extends JpaRepository<SeminarAttendanceEntity, Long> {

  Optional<SeminarAttendanceEntity> findBySeminarEntityAndMemberEntity(SeminarEntity seminarEntity,
      MemberEntity memberEntity);

  List<SeminarAttendanceEntity> findAllBySeminarEntityAndSeminarAttendanceStatusEntity(
      SeminarEntity seminar, SeminarAttendanceStatusEntity seminarAttendanceStatus);

}
