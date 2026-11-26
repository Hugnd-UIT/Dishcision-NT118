import { ContentSection } from '../components/content-section'
import { ProfileForm } from './profile-form'

export function SettingsProfile() {
  return (
    <ContentSection
      title='Hồ sơ'
      desc='Thông tin hiển thị công khai của bạn trên hệ thống.'
    >
      <ProfileForm />
    </ContentSection>
  )
}
