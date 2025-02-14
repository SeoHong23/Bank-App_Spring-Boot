package com.tenco.bank.service;

import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.WithdrawalDTO;
import com.tenco.bank.repository.interfaces.HistoryRepository;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.History;
import com.tenco.bank.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.AccountSaveDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;

import java.util.List;

@Service
public class AccountService {

    @Autowired // 가독성 때문에 둘다 작성
    private final AccountRepository accountRepository;
    @Autowired
    private final HistoryRepository historyRepository;

    public AccountService(AccountRepository accountRepository, HistoryRepository historyRepository) {
        this.accountRepository = accountRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * 계좌 생성 기능
     *
     * @param dto
     * @param pricipalId
     *
     */
    @Transactional //하나의 작업의 기준이되는거
    public void createAccount(AccountSaveDTO dto, Integer pricipalId) {
        try {
            // 바로 save, 조회하고 계좌 존재 시 알려주기
            accountRepository.insert(dto.toAccount(pricipalId));
        } catch (DataAccessException e) {
            // DB연결 및 제약 사항 위한 및 쿼리 오류
            throw new DataDeliveryException("잘못된 처리 입니다", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // 예외 처리 - 에러 페이지로 이동
            throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * 복잡한 Select 쿼리문일 경우 트랜잭션 처리를 해주 것이 좋습니다.
     * 여기서는 단순한 Select 구문이라 바로 진행 합니다.
     * @param principalId
     * @return
     */
    public List<Account> readAccountListByUserId(Integer principalId) {
        List<Account> accountListEntity = null;
        try {
            accountListEntity = accountRepository.findAllByUserId(principalId);
        } catch (DataAccessException e) {
            throw new DataDeliveryException("잘못된 처리 입니다", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // 예외 처리 - 에러 페이지로 이동
            throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
        }
        return accountListEntity;
    }

    // 출금 -->
    // 1. 트랜잭션 처리
    // 2. 계좌번호 존재 여부 확인 -- select
    // 3. 본인 계좌 여부 확인 -- 객체에서 확인 가능
    // 4. 계좌에 비밀번호 일치 여부 확인 -- 객체에서 확인가능
    // 5. 잔액 여부 확인(출금 가능 금액)
    // 6. 출금 처리 --> update
    // 7. 거래 내역 등록 --> insert(history)
    @Transactional
    public void updateAccountWithdraw(WithdrawalDTO dto, Integer principalId) {
        Account account = accountRepository.findByNumber(dto.getWAccountNumber());
        if(account == null) {
            throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
        }
        account.checkOwner(principalId); // 아니라면 exceptionhandler 실행
        account.checkPassword(dto.getWAccountPassword());
        account.checkBalance(dto.getAmount()); // 예외 처리 안빠짐
        account.withdraw(dto.getAmount()); // 객체 상태 변경
        accountRepository.updateById(account); // ==> update
        // History insert처리
        // History 객체 생성해서 넣기
         History history =new History();
         history.setAmount(dto.getAmount());
         history.setWBalance(account.getBalance());
         history.setDBalance(null);
         history.setWAccountId(account.getId());
         history.setDAccountId(null);
         int rowResetCount = historyRepository.insert(history);
         if(rowResetCount != 1) {
             throw new DataDeliveryException(Define.FAILED_PROCESSING,HttpStatus.INTERNAL_SERVER_ERROR);

         }
    }

    // 입금 기능 만들기
    // 1. 트랜잭션 처리
    // 2. 계좌 존재 여부 확인 --> select --> Account 모델 리턴
    // 3. 본인 계좌 여부 확인 --> 객체 상태값에서 확인 가능
    // 4. 입금 처리 --> update
    // 5. 거래내역 등록 --> history table --> insert
    @Transactional
    public void updateAccountDeposit(DepositDTO dto, Integer principalId) {
        Account account = accountRepository.findByNumber(dto.getDAccountNumber());
        if(account == null) {
            throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
        }
        account.checkOwner(principalId);
        accountRepository.updateById(account);

        History history = new History();
        history.setAmount(dto.getAmount());
        history.setDBalance(account.getBalance());
        history.setWBalance(null);
        history.setDAccountId(account.getId());
        history.setWAccountId(null);
        int rowResultCount = historyRepository.insert(history);
        if(rowResultCount != 1) {
            throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
