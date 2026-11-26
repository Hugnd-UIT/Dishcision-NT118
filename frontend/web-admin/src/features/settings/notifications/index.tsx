import { ContentSection } from '../components/content-section'
import { NotificationsForm } from './notifications-form'

export function SettingsNotifications() {
  return (
    <ContentSection
      title='Thông báo'
      desc='Cấu hình phương thức và loại thông báo bạn muốn nhận.'
    >
      <NotificationsForm />
    </ContentSection>
  )
}
