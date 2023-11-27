package com.example.hycare.Repository;

import com.example.hycare.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{
    private final EntityManager em;

    public Member findByEmail(String email) {
        Member member = null;
        try {
            TypedQuery<Member> query = em.createQuery("SELECT m from Member m where m.email = :email", Member.class)
                    .setParameter("email", email);
            List<Member> results = query.getResultList();

            if (!results.isEmpty()) {
                member = results.get(0);
            }
        } catch (NoResultException e) {
            // 데이터가 없을 때
            member = null;
        }
        return member;
    }
}
