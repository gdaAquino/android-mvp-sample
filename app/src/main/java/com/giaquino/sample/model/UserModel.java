package com.giaquino.sample.model;

import android.support.annotation.NonNull;
import com.giaquino.sample.model.api.GithubApi;
import com.giaquino.sample.model.db.contract.UserContract;
import com.giaquino.sample.model.entity.User;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * @author Gian Darren Azriel Aquino.
 */
public class UserModel {

    private UserContract.Dao userDao;
    private GithubApi githubApi;
    private Observable<List<User>> observableUsers;
    private PublishSubject<Throwable> observableErrors = PublishSubject.create();

    public UserModel(@NonNull UserContract.Dao userDao, @NonNull GithubApi githubApi) {
        this.userDao = userDao;
        this.githubApi = githubApi;
    }

    public void loadUsers(final int since) {
        githubApi.getUsers(GithubApi.GITHUB_TOKEN, since).subscribe(users -> {
            if (since == 0) {
                userDao.delete();
            }
            userDao.insert(users);
        }, throwable -> observableErrors.onNext(throwable));
    }

    public Observable<List<User>> users() {
        if (observableUsers == null) observableUsers = userDao.query();
        return observableUsers;
    }

    public Observable<Throwable> errors() {
        return observableErrors.debounce(UserContract.Dao.DEFAULT_DEBOUNCE, TimeUnit.MILLISECONDS);
    }
}
