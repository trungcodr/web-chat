
# GIT WORKFLOW

Quy trình quản lý Git cho dự án công nghệ thông tin (mức độ trung bình).

---

## 📦 1. Cấu trúc nhánh (Branch Structure)

- `main` / `master`: Nhánh chính, luôn chứa mã ổn định, có thể deploy.
- `develop`: Nhánh phát triển tổng hợp.
- `feature/*`: Nhánh phát triển tính năng mới (VD: `feature/login-api`).
- `hotfix/*`: Nhánh sửa lỗi khẩn cấp (VD: `hotfix/fix-payment-crash`).
- `release/*`: Nhánh chuẩn bị phát hành (VD: `release/v1.2.0`).

---

## 🛠 2. Quy tắc commit
- Require: Trước khi commit, cần đảm bảo
    - Đảm bảo pass toàn bộ unit test
    - Đảm bảo install && package thành công

- Format:
  ```
  <type>: <short description>
  ```
  VD:
    - `feat: thêm API đăng nhập`
    - `fix: sửa lỗi crash khi load danh sách`
    - `refactor: tối ưu component Header`

- Không commit file rác → dùng `.gitignore` chặn:
    - file build (`/build/`, `/dist/`),
    - log (`*.log`),
    - IDE config (`.idea/`, `.vscode/`).

---

## 🔀 3. Quy trình merge

- Merge vào `develop` / `master` qua Merge Request/Pull Request.
- MR/PR yêu cầu ít nhất **1 người review**.
- Check:
    - Đạt requirement.
    - Không phá tính năng khác.
    - Code sạch, không log/debug.
    - Viết unit test nếu cần.

---

## 🧪 4. Quy trình kiểm thử

- Merge `develop`: kiểm thử nội bộ (dev test, staging).
- Merge `release`/`master`: kiểm thử QA/UAT.

---

## 🚀 5. Quy trình release

1. Từ `develop` → tạo `release/x.y.z`.
2. QA test trên release.
3. OK:
    - Merge `release` → `master`.
    - Merge `release` → `develop` (ngược lại để tránh mất code).
    - Tag version:
      ```bash
      git tag v1.2.0
      git push origin v1.2.0
      ```

---

## 🛡 6. Quy trình hotfix

1. Từ `master` → tạo `hotfix/*`.
2. Sửa, test, merge `hotfix` → `master`.
3. Merge `hotfix` → `develop`.

---

## 🔄 7. Quy trình pull, rebase

### Trước khi code:
```bash
git checkout develop
git pull origin develop
git checkout feature/ten-tinh-nang
git rebase develop
```

### Trước khi mở MR/PR:
```bash
git checkout develop
git pull origin develop
git checkout feature/ten-tinh-nang
git rebase develop
```

---

## ⚔ 8. Fix conflict khi rebase

1. Chạy:
   ```bash
   git rebase develop
   ```
2. Nếu conflict:
    - Mở file, tìm:
      ```
      <<<<<<< HEAD
      (code develop)
      =======
      (code feature)
      >>>>>>> ten-commit
      ```
    - Giữ lại code đúng.
    - Xóa các dấu `<<<<<<<`, `=======`, `>>>>>>>`.

3. Đánh dấu đã fix:
   ```bash
   git add <file>
   git rebase --continue
   ```

4. Nếu rối:
   ```bash
   git rebase --abort
   ```

5. Sau khi xong:
   ```bash
   git push origin feature/ten-tinh-nang --force
   ```

---

## 🏹 9. Khi nào dùng rebase, khi nào dùng merge?

| Trường hợp                            | Dùng gì            |
|---------------------------------------|---------------------|
| Đồng bộ code mới từ develop vào feature | `git rebase develop` |
| Merge feature xong vào develop         | MR/PR + merge      |
| Hotfix gấp vào master                   | merge (hoặc cherry-pick) |

---

## 💥 10. Anti-pattern cần tránh

- ❌ Commit lung tung (`fix bug`, `update`).
- ❌ Push code chưa test.
- ❌ Force push lên `develop` / `master`.
- ❌ Merge lung tung, không MR/PR.
- ❌ Conflict → xóa hết code người khác để “cho lẹ”.

---

## 🌍 11. Công cụ đề xuất

- Code review: GitHub, GitLab, Bitbucket.
- CI/CD: GitHub Actions, GitLab CI, Jenkins.
- Issue tracker: Jira, Trello.


# 📏 Yêu cầu squash commit khi merge

- Khi merge vào `develop` hoặc `master`, **mỗi nhánh chỉ có 1 commit duy nhất**.

### Lý do:
✅ Giúp lịch sử Git ngắn gọn, dễ đọc.  
✅ Review lịch sử dễ phát hiện tính năng, bug fix.  
✅ Tránh spam lịch sử bởi các commit nhỏ (`fix typo`, `update text`, v.v.).

### 💣 Cách squash commit (thủ công)

1️⃣ Kiểm tra số commit trên nhánh:
```bash
git log develop..feature/ten-tinh-nang --oneline
```

2️⃣ Squash toàn bộ commit:
```bash
git rebase -i develop
```

3️⃣ Trong file hiện ra:
- Giữ dòng đầu `pick`.
- Các dòng dưới sửa thành `squash` hoặc `s`.
- Save + close.

4️⃣ Sửa message cuối cùng → ghi nội dung ngắn gọn, rõ ràng.

5️⃣ Force push:
```bash
git push origin feature/ten-tinh-nang --force
```

### 💥 Squash qua MR/PR

- Trên GitHub:
    - Khi merge, chọn **"Squash and merge"**.

- Trên GitLab:
    - Bật tùy chọn **"Squash commits when merging"** trong MR.

### ❗ Lưu ý

- Chỉ squash commit trên nhánh feature/release, **KHÔNG squash history của nhánh develop/main**.
- Sau squash, force push cẩn thận, chỉ push nhánh của mình.