package com.member.service;

import com.member.address.Address;
import com.member.member.Member;
import com.member.member.MemberEntity;
import com.member.exceptions.MemberServiceException;
import com.member.exceptions.ErrorMessages;
import com.member.model.MemberUpdateQueryModel;
import com.member.repositories.MemberRepository;
import com.member.utils.Utils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    Utils utils;

    @Override
    public List<MemberEntity> getMembers(int page, int limit) {
        List<MemberEntity> returnValue = new ArrayList<>();
        PageRequest pageableRequest = PageRequest.of(page, limit);
        Page<MemberEntity> bookPage = memberRepository.findAll(pageableRequest);
        List<MemberEntity> books = bookPage.getContent();

        for (MemberEntity book: books) {
            MemberEntity MemberEntity = new MemberEntity();
            BeanUtils.copyProperties(book, MemberEntity);
            returnValue.add(MemberEntity);
        }

        return returnValue;
    }

    @Override
    public Member createMember(Member member) {
        if(memberRepository.findByFirstName(member.getEmail()) != null) throw new RuntimeException("member already Exist");

        for(Address address: member.getAddresses()) {
            address.setMemberDetails(member);
        }

        ModelMapper modelMapper = new ModelMapper();
        MemberEntity memberEntity;
        memberEntity = modelMapper.map(member, MemberEntity.class);
        memberEntity.setPassword(bCryptPasswordEncoder.encode(member.getPassword()));
        MemberEntity savedBookDetails = memberRepository.save(memberEntity);
        Member returnValue = modelMapper.map(savedBookDetails, Member.class);
        return returnValue;
    }

    @Override
    public Member getMemberById(long id) throws NoResultException{
          Member returnValue = new Member();
        MemberEntity MemberEntity = memberRepository.findMemberById(id);
        BeanUtils.copyProperties(MemberEntity, returnValue);
        return returnValue;
    }

    @Override
    public Member updateMember(long id, MemberUpdateQueryModel book) throws MemberServiceException {
        Member returnValue = new Member();
        MemberEntity result = memberRepository.findMemberById(id);
        if(result == null) throw new MemberServiceException(ErrorMessages.BOOK_IS_NOT_FOUND.getErrorMessage());
       result.setFirstName(book.getFirstName());
       result.setLastName(book.getLastName());
       result.setSerialNumber(book.getSerialNumber());
       BeanUtils.copyProperties(result, returnValue);
        return returnValue;
    }

    @Override
    public void deleteMember(long id) {
        MemberEntity result = memberRepository.findMemberById(id);
        if(result == null) throw new MemberServiceException(ErrorMessages.BOOK_IS_NOT_FOUND.getErrorMessage());
        memberRepository.delete(result);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
            MemberEntity memberEntity = memberRepository.findByEmail(email);
            if (memberEntity == null) throw  new UsernameNotFoundException(email);
        return new User(memberEntity.getEmail(), memberEntity.getPassword(), new ArrayList<>());
    }

    @Override
    public Member getMember(String email) {
        MemberEntity memberEntity = memberRepository.findByEmail(email);
        if(memberEntity == null) throw  new UsernameNotFoundException(email);
        Member returnValue = new Member();
        BeanUtils.copyProperties(memberEntity, returnValue);
        return returnValue;
    }
}
