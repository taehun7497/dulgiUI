package com.korea.dulgiUI.question;

import com.korea.dulgiUI.answer.Answer;
import com.korea.dulgiUI.category.Category;
import com.korea.dulgiUI.error.DataNotFoundException;
import com.korea.dulgiUI.uesr.SiteUser;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor // 롬복이 제공하는 애너테이션으로 final이 붙은 속성을 포함하는 생성자를 자동으로 생성합니다.
@Service // 스프링에게 해당 클래스가 비즈니스 로직을 담당하는 서비스 클래스임을 알려줍니다.
public class QuestionService {

    private final QuestionRepository questionRepository; // QuestionRepository 의존성을 주입받습니다.

    // 질문 목록을 페이징하여 반환하는 메서드입니다.
    public Page<Question> getList(int page,
                                  String kw,
                                  Category category) {

        // 정렬 조건을 생성합니다. (작성일자 기준으로 내림차순 정렬)
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        // 페이징을 위한 페이지 요청 객체를 생성합니다.
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        // 검색 조건을 생성합니다.
        Specification<Question> spec = search(kw, category.getId());
        // Specification을 사용하여 질문을 조회하고 페이징하여 반환합니다.
        return this.questionRepository.findAll(spec, pageable);
    }

    // 질문의 ID를 기반으로 특정 질문을 조회하는 메서드입니다.
    public Question getQuestion(Integer id) {

        // 주어진 ID에 해당하는 질문을 데이터베이스에서 조회합니다.
        Optional<Question> question = this.questionRepository.findById(id);

        // 조회된 질문이 존재하는 경우에는 질문 객체를 반환합니다.
        // 존재하지 않는 경우에는 DataNotFoundException을 발생시킵니다.
        if (question.isPresent()) {
            return question.get();
        } else {
            throw new DataNotFoundException("question Not Found");
        }
    }

    // 특정 사용자가 작성한 모든 질문 목록을 조회하는 메서드입니다.
    public List<Question> getQuestions(SiteUser user) {

        // 특정 사용자가 작성한 모든 질문 목록을 데이터베이스에서 조회합니다.
        Optional<List<Question>> questions = this.questionRepository.findAllByAuthor(user);

        // 조회된 질문 목록이 존재하는 경우에는 해당 목록을 반환합니다.
        // 존재하지 않는 경우에는 DataNotFoundException을 발생시킵니다.
        if (questions.isPresent()) {
            return questions.get();
        } else {
            throw new DataNotFoundException("question Not Found");
        }
    }

    // 새로운 질문을 생성하는 메서드입니다.
    public void create(String subject,
                       String content,
                       SiteUser author,
                       Category category) {

        // 새로운 질문 객체를 생성하고 속성값을 설정합니다.
        Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
        q.setCategory(category);
        q.setCreateDate(LocalDateTime.now());
        q.setAuthor(author);
        // 생성된 질문을 저장합니다.
        this.questionRepository.save(q);
    }

    // 질문을 수정하는 메서드입니다.
    public void modify(Question question,
                       String subject,
                       String content) {

        // 주어진 질문의 제목과 내용을 수정하고 수정일자를 갱신합니다.
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        // 수정된 질문을 저장합니다.
        this.questionRepository.save(question);
    }

    // 질문을 삭제하는 메서드입니다.
    public void delete(Question question) {

        // 주어진 질문을 삭제합니다.
        this.questionRepository.delete(question);
    }

    // 질문에 대한 투표를 처리하는 메서드입니다.
    public void vote(Question question,
                     SiteUser siteUser) {

        // 주어진 사용자가 질문에 투표한 것으로 처리하고, 변경 사항을 저장합니다.
        question.getVoter().add(siteUser);
        this.questionRepository.save(question);
    }

    // 검색 조건을 생성하는 메서드입니다.
    private Specification<Question> search(String kw,
                                           int categoryId) {

        // 동적 검색 조건을 생성하여 반환합니다.
        return new Specification<>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Question> q,
                                         CriteriaQuery<?> query,
                                         CriteriaBuilder builder) {

                query.distinct(true); // 중복 제거
                Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
                return builder.and(builder.equal(q.get("category").get("id"), categoryId),
                        builder.or(builder.like(q.get("subject"), "%" + kw + "%"), // 제목
                                builder.like(q.get("content"), "%" + kw + "%"), // 내용
                                builder.like(u1.get("username"), "%" + kw + "%"), // 질문 작성자
                                builder.like(a.get("content"), "%" + kw + "%"), // 답변 내용
                                builder.like(u2.get("username"), "%" + kw + "%"))
                );
            }
        };
    }

    // 질문의 조회수를 증가시키는 메서드입니다.
    public void incrementView(Question question) {
        // 질문의 조회수를 1 증가시키고, 변경 사항을 저장합니다.
        question.setView(question.getView() + 1);
        this.questionRepository.save(question);
    }
}