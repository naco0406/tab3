# NAGA, 나가

<img src="https://github.com/naco0406/tab3/assets/71596178/a353ec70-a146-41ba-abd9-039cab8b3857.png" width="100%" height="100%"/>
<img src="https://github.com/naco0406/tab3/assets/71596178/a19b120e-5a51-430e-8c0f-4b7be233e061" width="100%" height="100%"/>
<img src="https://github.com/naco0406/tab3/assets/71596178/f0c822c5-5d48-492a-8697-06fec4bc968a.png" width="100%" height="100%"/>
<img src="https://github.com/naco0406/tab3/assets/71596178/49c9a740-0b36-4817-9496-301b73a7e851.png" width="100%" height="100%"/>
<img src="https://github.com/naco0406/tab3/assets/71596178/56ee867a-6a38-4131-aa10-7f726cf2261a.png" width="100%" height="100%"/>


## 💻 프로젝트 소개

Android Studio(Kotlin)를 사용하여 개발한 여행에서 만난 사람들의 연락처를 저장하고, 위치를 태그하는 어플리케이션

## 개발환경

- Language : Kotlin
- OS : Android
- IDE : Android Studio

> minSdkVersion 21
>
> targetSdkVersion 32


## APK 파일

## 👫 팀원

고영 - 카이스트 전산학부 21학번

한채연 - 숙명여대 IT공학과 20학번

## 📌 주요 기능

### Tab1

> 연락처 검색, 추가/수정/삭제 가능
> 

<img src="https://github.com/naco0406/tab3/assets/71596178/56add349-6c37-46ed-b312-6c5c20964017.png" width="30%" height="30%"/>
<img src="https://github.com/naco0406/tab3/assets/71596178/1568cf4e-935d-4c1d-90ae-b155f649eb4f.png" width="30%" height="30%"/>


- 연락처 정보 보여주기
- 사용자 이름, 전화번호로 검색
- 이미지, 이름, 전화번호를 입력하여 새로운 프로필 생성
- 기존 연락처 수정 및 삭제
- 전화걸기, 메시지 보내기 기능

---

> 기술 설명

- RecyclerView와 ProfileAdapter를 사용해 JSON 데이터 형태인 연락처 리스트 보여주기
- 사용자 이미지 효율적으로 처리하기 위해 Glide 라이브러리 이용
- 연락처는 `sortByName` 으로 정렬
- SearchView에서 검색어 입력시마다 `onQueryTextChange` 가 호출되어 연락처, 사용자 이름으로 검색
- JSON 데이터 파싱을 간단하게 하기 위해서 GSON 라이브러리 사용
- JSON데이터가 변경될 때마다 `notifyDataSetChanged()`로 RecyclerView 갱신
- Intent를 사용해 전화 다이얼과 문자 보내기로 이동

### Tab2

> 캘린더에서 선택한 날짜에 맞는 데이터와 이미지 보여주기

<p align="center">
<img src="https://github.com/naco0406/tab3/assets/71596178/e6984da7-64e2-4d84-a3e5-cd92d1ff6207" width="30%" height="30%">
</p>

<img src="https://github.com/naco0406/tab3/assets/71596178/55a52592-16db-4949-aad3-a65ad7dc982c.png" width="30%" height="30%"/>
<img src="https://github.com/naco0406/tab3/assets/71596178/07e03fa9-bd36-47af-b510-85f5ab92c70b.png" width="30%" height="30%"/>



- 사용자가 ButtomSheetBehavior를 올리고 내리는 정도에 따라 CardView의 이미지 조정
- 캘린더에서 데이터가 있는 날짜에 빨간 점 표시
- Custom Dialog로 원하는 년, 월 선택

---

> 기술 설명
> 
- Material CalendarView를 사용하여 커스텀 캘린더 구현
- ButtomSheetBehavior를 사용하여 하단에서 올라오는 창 구현
- Number Picker를 사용하여 원하는 년, 월 선택

### Tab3

> 사진을 모아 보고, 추가할 수 있는 갤러리


<img src="https://github.com/naco0406/tab3/assets/71596178/5f5f337a-bc6a-47be-b93b-dd6e5896c7cb.png" width="30%" height="30%"/>
<img src="https://github.com/naco0406/tab3/assets/71596178/1df8ffb3-2feb-43c1-a554-792089640d4c.png" width="30%" height="30%"/>


- 사진 추가 클릭시 카메라 앱으로 이동하고, 사진 찍기
- 찍은 사진과 함께 사용자의 현재 위치와 현재 시간 자동 입력 및 연락처에 있는 사람 선택
- GPS를 이용하여 도시와 국가를 자동 입력
- 장소 필터링 기능
- 좋아요 기능

---

> 기술 설명
> 
- 이미지 크기를 정사각형으로 조절해 GridLayout으로 갤러리 사진 보여주기
- 이미지 선택시 Modal창이 떠서 사진과 데이터를 띄워줌
- 사진에 함께한 사람을 태그하는 Custom Item Selector 구현

