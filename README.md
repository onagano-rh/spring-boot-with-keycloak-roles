# Spring Boot with Keycloak Roles

Keycloakプロジェクト提供のアダプタ (org.keycloak:keycloak-spring-boot-starter) がdeprecatedになったため、
Spring Bootの利用者はSpring標準のSpring SecurityによってKeycloakと通信するよう改修する必要がある。

そのSpring Securityでロールを利用するには通常のサンプルでは扱われていないようなカスタマイズが必要になる。
これはKeycloakで定義するロールがOAuthやOpenID Connect (OIDC)の標準のクレイムではないためで、
それを利用するには自らトークン内のロールを表す特定のクレイムを読み込み、
Spring Securityにおいて認可の概念を表す `GrantedAuthority` オブジェクトに変換しなければならない。

KeycloakはOIDC準拠なので、単なるOAuthではなくOIDCのクライアントとして使うことにする。
OIDCではIDトークンにより認証するが、Keycloakのデフォルトではロールの情報はアクセストークンにしか含まれないため、
IDトークンにも含まれるようにKeycloakの設定を変更する必要がある。
こうすることで `.requestMatchers("/protected/normal").hasAnyRole("admin", "normal")` といった
Spring SecurityのDSLで宣言的に認可を行うことができる。



## 準備

- Java 11 or 17
- [Keycloak 22.0.z](https://www.keycloak.org/downloads)

Red Hat Build of Keycloak 22 がまだリリースされていないのでこのコミュニティ版で代用する。

アーカイブを解凍し、ポート番号を8180にして起動する。

```
${KEYCLOAK_HOME}/bin/kc.sh start-dev --http-port=8180
```

masterレルムの管理ユーザを登録ログインし、サンプルアプリで使用するレルムとその専用管理ユーザを作成する。

- レルム "test-realm" を作成
- そのレルム内にユーザ "test-admin" を作成
  - Credentialsタブでパスワードを "password" に設定
  - Role mappingタブで "realm-management" クライアントの "realm-admin" ロールをアサイン

以降はこのレルムしか使わないのでmasterレルムからはログアウトし、
以下のtest-realm専用管理コンソールにtest-adminユーザでログインするとよい。

- http://localhost:8180/admin/test-realm/console



## 利用するSpring Bootのコンポーネント

Spring BootにおいてOAuthを扱うコンポーネントは二つある。

- OAuth2 Client (spring-boot-starter-web)
- OAuth2 Resource Server (spring-boot-starter-oauth2-resource-server)

保護された領域にアクセスしたとき、Keycloakにリダイレクトさせてログイン画面を出したい場合は前者を、
300番台のリダイレクトレスポンスではなく単に400番台のエラーレスポンスを返したい場合は後者を選択する。

典型的には、通常のWebアプリでは前者を、JSONのみを返すREST APIサーバでは後者を使うことになる。
後者はトークンを受け取り検証するだけだが、前者はトークンの発行にも関わる点が異なる。

本リポジトリのサンプルアプリは前者を使っているが、簡便のためレスポンスはHTMLではなくJSONにしている。
JSONを返すからといってログイン画面へのリダイレクトができないcurlコマンドでは利用できないので、
必ずブラウザでアクセスする必要がある。



## サンプルアプリの概要

```
./mvnw clean spring-boot:run
```

上記のコマンドによってビルドおよび起動し、以下のエンドポイントを持つ。

- http://localhost:8080/
  - 認証なしでもアクセス可能
- http://localhost:8080/public
  - 認証なしでもアクセス可能
- http://localhost:8080/protected
  - 認証を済ませないとアクセスできない
- http://localhost:8080/protected/normal
  - 認証を済ませた上、"normal"もしくは"admin"ロールがないとアクセスできない
- http://localhost:8080/protected/admin
  - 認証を済ませた上、"admin"ロールがないとアクセスできない
- http://localhost:8080/inspect
  - デバッグ用のエンドポイント。現在の`Authentication`オブジェクトの中身を表示する



## test-realmの構築

サンプルアプリを動かすために必要なグループ、ユーザ、ロールの作成、ロールの割り当て、クライアント登録を行う。
管理コンソールで手作業で行うこともできるが、 `kcadm.sh` による自動化の例としてスクリプトを用意している。

```
./populate-realm.sh
```

これは以下のようにtest-realmを構築する。

- Group "Team 01" (with role "normal" and "admin" assigned)
  - User "testuser01"
  - User "testuser02"
  - User "testuser03"
- Group "Team 01" (with role "admin" assigned)
  - User "testuser04"
  - User "testuser05"
  - User "testuser06"

グループに割り当てたロールはそのグループ内のユーザにも継承される。
よってサンプルアプリで以下のような動きを確認できる。

- "testuser01" は "/protected/normal" にも "/protected/admin" にもアクセスできる
- "testuser04" は "/protected/normal" にはアクセスできるが "/protected/admin" にはアクセスできない
- "test-admin" は "/protected" にはアクセスできるが、適切なロールがないので "/protected/*" にはアクセスできない

グループ（とユーザ）の名前は固有名詞だが、ロールの名前は一般名詞（しばしば動詞）になる。
アプリケーションは固有名詞をハードコードすることは通常ないので、ロールという概念が必要になってくる。

外部システムからユーザを取り込む場合は、mapperを適切に設定することでグループの設定も自動で行わせる事ができる。
ロールはアプリケーションに固有の概念なので、レルム管理者が事前定義するか、
アプリケーション開発者がレルム管理者に作成と適切なアサインの依頼を行うことになる。



## 発展的な内容

- ロールはレルムごとではなくクライアントごとに定義することもできる
  - スクリプトで設定した"normal"や"admin"はレルムロールである
  - レルムロールのトークン内のクレイム名は `realm_access.roles` だが、クライアントロールは `resource_access.<client_id>.roles` である
  - `MySecurityConfig.java` 内では realm_access.roles` を拾うようにしかコーディングしていないので、クライアントロールも取り込むには改修が必要になる



## 参考リンク

- Spring Securityの公式ドキュメント
  - https://docs.spring.io/spring-security/reference/reactive/oauth2/index.html
- Spring Securityの公式サンプルコード
  - https://github.com/spring-projects/spring-security-samples
- 解説記事1 (英語)
  - https://dzone.com/articles/spring-boot-3-keycloak
  - OAuth2 ClientではなくResource Serverの例だが、ロールを扱っている
- 解説記事2 (英語)
  - https://developers.redhat.com/articles/2023/07/24/how-integrate-spring-boot-3-spring-security-and-keycloak
  - OAuth2 Clientの例だが、ロールは扱っていない
- 解説記事3 (日本語)
  - https://kazuhira-r.hatenablog.com/entry/2022/09/08/015028
  - ロールを扱っている
- 私的別作
  - https://github.com/onagano-rh/jbosswork/tree/master/rhsso-msapps-sb
  - 解説記事3を参考にロールを扱っている
  - OAuth2 Clientの ui-frontend とResource Serverの api-backend の二つのサーバからなる
  - ui-frontednではapi-backendへのアクセスにアクセストークンを再利用している

