package com.korea.dulgiUI.answer;


import com.korea.dulgiUI.error.DataNotFoundException;
import com.korea.dulgiUI.question.Question;
import com.korea.dulgiUI.uesr.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AnswerService {

    public static final String RECENT_ORDER = "recent"; // 답변 목록을 최신순으로 정렬하기 위한 상수
    public static final String RECOMMEND_ORDER = "recommend"; // 답변 목록을 추천순으로 정렬하기 위한 상수
    private final AnswerRepository answerRepository; // Answer 엔티티와 상호작용하기 위한 Repository

    public Page<Answer> getList(int page,
                                Question question,
                                String so) {

        List<Sort.Order> sorts = new ArrayList<>(); // 정렬 조건을 담을 리스트 생성

        if (RECOMMEND_ORDER.equals(so)) { // 정렬 순서가 "recommend"인 경우
            sorts.add(Sort.Order.desc("voter")); // 추천순으로 정렬
        } else { // 그 외의 경우, 즉 "recent"인 경우
            sorts.add(Sort.Order.desc("createDate")); // 최신순으로 정렬
        }
        Pageable pageable = PageRequest.of(page, 3, Sort.by(sorts)); // 페이지와 정렬 조건으로 페이지 요청 생성
        return this.answerRepository.findAllByQuestion(question, pageable); // 질문에 해당하는 답변 목록을 페이지별로 가져와 반환
    }

    public List<Answer> getListByAuthor(SiteUser user) {
        Optional<List<Answer>> answers = this.answerRepository.findAllByAuthor(user); // 사용자가 작성한 답변 목록을 옵셔널로 가져옴

        if (answers.isPresent()) { // 가져온 답변 목록이 존재하는 경우
            return answers.get(); // 답변 목록 반환
        } else { // 가져온 답변 목록이 비어있는 경우
            throw new DataNotFoundException("answers not found"); // 데이터를 찾을 수 없다는 예외 발생
        }
    }

    public Answer create(Question question,
                         String content,
                         SiteUser author) {

        Answer answer = new Answer(); // 새로운 답변 객체 생성
        answer.setContent(content); // 내용 설정
        answer.setCreateDate(LocalDateTime.now()); // 생성일 설정
        answer.setQuestion(question); // 질문 설정
        answer.setAuthor(author); // 작성자 설정
        this.answerRepository.save(answer); // 답변 저장
        return answer; // 생성된 답변 반환
    }

    public Answer getAnswer(Integer id) {
        Optional<Answer> answer = this.answerRepository.findById(id); // 주어진 ID에 해당하는 답변을 옵셔널로 가져옴

        if (answer.isPresent()) { // 가져온 답변이 존재하는 경우
            return answer.get(); // 해당 답변 반환
        } else { // 가져온 답변이 비어있는 경우
            throw new DataNotFoundException("answer not found"); // 데이터를 찾을 수 없다는 예외 발생
        }
    }
    public void modify(Answer answer,
                       String content) {

        answer.setContent(content); // 내용 수정
        answer.setModifyDate(LocalDateTime.now()); // 수정일 설정
        this.answerRepository.save(answer); // 답변 저장
    }

    public void delete(Answer answer) {
        this.answerRepository.delete(answer); // 답변 삭제
    }

    public void vote(Answer answer,
                     SiteUser siteUser) {

        answer.getVoter().add(siteUser); // 투표한 사용자를 답변의 투표자 목록에 추가
        this.answerRepository.save(answer); // 답변 저장
    }
}