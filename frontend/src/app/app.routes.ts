import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { AccountLoginComponent } from './pages/account/account-login.component';
import { AccountPasswordComponent } from './pages/account/account-password.component';
import { AccountRemoveComponent } from './pages/account/account-remove.component';
import { LoginComponent } from './pages/auth/login.component';
import { RegisterComponent } from './pages/auth/register.component';
import { RestoreComponent } from './pages/auth/restore.component';
import { RestorePasswordComponent } from './pages/auth/restore-password.component';
import { RestoreSuccessComponent } from './pages/auth/restore-success.component';
import { EditCertificatesComponent } from './pages/edit/edit-certificates.component';
import { EditCoursesComponent } from './pages/edit/edit-courses.component';
import { EditEducationComponent } from './pages/edit/edit-education.component';
import { EditHobbiesComponent } from './pages/edit/edit-hobbies.component';
import { EditLanguagesComponent } from './pages/edit/edit-languages.component';
import { EditLayoutComponent } from './pages/edit/edit-layout.component';
import { EditPasswordComponent } from './pages/edit/edit-password.component';
import { EditPracticsComponent } from './pages/edit/edit-practics.component';
import { EditProfileComponent } from './pages/edit/edit-profile.component';
import { EditSkillsComponent } from './pages/edit/edit-skills.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { SearchComponent } from './pages/search/search.component';
import { WelcomeComponent } from './pages/welcome/welcome.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'welcome' },
  { path: 'welcome', component: WelcomeComponent },
  { path: 'search', component: SearchComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'restore', component: RestoreComponent },
  { path: 'restore/success', component: RestoreSuccessComponent },
  { path: 'restore/:token', component: RestorePasswordComponent },
  { path: 'account/password', component: AccountPasswordComponent, canActivate: [AuthGuard] },
  { path: 'account/login', component: AccountLoginComponent, canActivate: [AuthGuard] },
  { path: 'account/remove', component: AccountRemoveComponent, canActivate: [AuthGuard] },
  {
    path: 'edit',
    component: EditLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'profile' },
      { path: 'profile', component: EditProfileComponent },
      { path: 'contacts', component: EditProfileComponent },
      { path: 'info', component: EditProfileComponent },
      { path: 'photo', component: EditProfileComponent },
      { path: 'password', component: EditPasswordComponent },
      { path: 'skills', component: EditSkillsComponent },
      { path: 'practics', component: EditPracticsComponent },
      { path: 'education', component: EditEducationComponent },
      { path: 'courses', component: EditCoursesComponent },
      { path: 'languages', component: EditLanguagesComponent },
      { path: 'certificates', component: EditCertificatesComponent },
      { path: 'hobbies', component: EditHobbiesComponent }
    ]
  },
  { path: ':uid', component: ProfileComponent },
  { path: '**', redirectTo: 'welcome' }
];
