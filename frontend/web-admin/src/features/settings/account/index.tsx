import { ContentSection } from '../components/content-section'
import { AccountForm } from './account-form'

export function SettingsAccount() {
  return (
    <ContentSection
      title='Tài khoản'
      desc='Cập nhật cài đặt tài khoản. Thiết lập ngôn ngữ và thông tin cá nhân.'
    >
      <AccountForm />
    </ContentSection>
  )
}
